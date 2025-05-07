package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authorization.domain.*;
import com.netgrif.application.engine.authorization.domain.constants.GroupConstants;
import com.netgrif.application.engine.authorization.domain.params.GroupParams;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
import com.netgrif.application.engine.authorization.domain.repositories.CaseRoleRepository;
import com.netgrif.application.engine.authorization.domain.repositories.ProcessRoleRepository;
import com.netgrif.application.engine.authorization.domain.repositories.RoleAssignmentRepository;
import com.netgrif.application.engine.authorization.service.interfaces.IGroupService;
import com.netgrif.application.engine.authorization.service.interfaces.IUserService;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.ApplicationRoleRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.State;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
class TaskAuthorizationServiceTest {

    @Autowired
    private TaskAuthorizationService authorizationService;

    @Autowired
    private ISessionManagerService sessionManagerService;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @Autowired
    private CaseRoleRepository caseRoleRepository;

    @Autowired
    private ApplicationRoleRunner applicationRoleRunner;

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IUserService userService;

    @Autowired
    private IGroupService groupService;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IDataService dataService;

    private Identity testIdentity;

    private Group testGroup;

    private Case testCase;

    private Case testCaseWithDefault;

    @BeforeEach
    public void beforeEach() throws IOException, MissingPetriNetMetaDataException {
        testHelper.truncateDbs();

        testIdentity = importHelper.createIdentity(IdentityParams.with()
                .username(new TextField("username"))
                .password(new TextField("password"))
                .firstname(new TextField("firstname"))
                .lastname(new TextField("lastname"))
                .build(), new ArrayList<>());

        Process testProcess = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/task_authorization_test.xml"),
                VersionType.MAJOR, sessionManagerService.getLoggedSystemIdentity().getActiveActorId()).getNet();
        Process testProcessWithDefault = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/task_authorization_default_test.xml"),
                VersionType.MAJOR, sessionManagerService.getLoggedSystemIdentity().getActiveActorId()).getNet();

        TestHelper.login(testIdentity);

        testCase = importHelper.createCase("test", testProcess);
        testCaseWithDefault = importHelper.createCase("test with default role", testProcessWithDefault);
    }

    @Test
    void canCallAssign() {
        // order of assertions is important!
        assert !authorizationService.canCallAssign(null);

        String taskId = testCase.getTaskStringId("t_assign");

        assert !authorizationService.canCallAssign(taskId);
        assert authorizationService.canCallAssign(testCaseWithDefault.getTaskStringId("t_assign"));

        ProcessRole posProcessRole = processRoleRepository.findByImportId("pos_process_role");
        assignProcessRole(testIdentity.getMainActorId(), posProcessRole);
        assert authorizationService.canCallAssign(taskId);

        updateAssigneeOfTask(taskId, new ObjectId().toString());
        assert !authorizationService.canCallAssign(taskId);
        updateAssigneeOfTask(taskId, null);

        ProcessRole negProcessRole = processRoleRepository.findByImportId("neg_process_role");
        assignProcessRole(testIdentity.getMainActorId(), negProcessRole);
        assert !authorizationService.canCallAssign(taskId);

        CaseRole posCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "pos_case_role");
        assignCaseRole(testIdentity.getMainActorId(), posCaseRole);
        assert authorizationService.canCallAssign(taskId);

        CaseRole negCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "neg_case_role");
        assignCaseRole(testIdentity.getMainActorId(), negCaseRole);
        assert !authorizationService.canCallAssign(taskId);

        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        assignAppRole(testIdentity.getMainActorId(), adminAppRole);
        assert authorizationService.canCallAssign(taskId);

        TestHelper.logout();
        assert !authorizationService.canCallAssign(taskId);
    }

    @Test
    void canCallAssignByGroups() {
        // order of assertions is important!
        User testUser = initializeTestUserWithGroup();

        assert !roleAssignmentRepository.findAllByActorId(testUser.getStringId()).iterator().hasNext();

        assert !authorizationService.canCallAssign(null);

        String taskId = testCase.getTaskStringId("t_assign");

        assert !authorizationService.canCallAssign(taskId);
        assert !authorizationService.canCallAssign(testCaseWithDefault.getTaskStringId("t_assign"));

        testGroup = updateGroupWithParent(testGroup, groupService.getDefaultGroup().getStringId());

        assert !authorizationService.canCallAssign(taskId);
        assert authorizationService.canCallAssign(testCaseWithDefault.getTaskStringId("t_assign"));

        ProcessRole posProcessRole = processRoleRepository.findByImportId("pos_process_role");
        assignProcessRole(testGroup.getStringId(), posProcessRole);
        assert authorizationService.canCallAssign(taskId);

        updateAssigneeOfTask(taskId, new ObjectId().toString());
        assert !authorizationService.canCallAssign(taskId);
        updateAssigneeOfTask(taskId, null);

        ProcessRole negProcessRole = processRoleRepository.findByImportId("neg_process_role");
        assignProcessRole(testGroup.getStringId(), negProcessRole);
        assert !authorizationService.canCallAssign(taskId);

        CaseRole posCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "pos_case_role");
        assignCaseRole(testGroup.getStringId(), posCaseRole);
        assert authorizationService.canCallAssign(taskId);

        CaseRole negCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "neg_case_role");
        assignCaseRole(testGroup.getStringId(), negCaseRole);
        assert !authorizationService.canCallAssign(taskId);

        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        assignAppRole(testGroup.getStringId(), adminAppRole);
        assert authorizationService.canCallAssign(taskId);

        TestHelper.logout();
        assert !authorizationService.canCallAssign(taskId);
    }

    @Test
    public void canCallCancel() {
        // order of assertions is important!
        assert !authorizationService.canCallCancel(null);

        String taskId = testCase.getTaskStringId("t_cancel");
        String taskIdWithDefault = testCaseWithDefault.getTaskStringId("t_cancel");

        assert !authorizationService.canCallCancel(taskId);

        updateAssigneeOfTask(taskIdWithDefault, sessionManagerService.getActiveActorId());
        assert authorizationService.canCallCancel(taskIdWithDefault);

        updateAssigneeOfTask(taskId, new ObjectId().toString());
        assert !authorizationService.canCallCancel(taskId);

        ProcessRole posProcessRole = processRoleRepository.findByImportId("pos_process_role");
        assignProcessRole(testIdentity.getMainActorId(), posProcessRole);
        assert !authorizationService.canCallCancel(taskId);

        updateAssigneeOfTask(taskId, sessionManagerService.getActiveActorId());
        assert authorizationService.canCallCancel(taskId);

        ProcessRole negProcessRole = processRoleRepository.findByImportId("neg_process_role");
        assignProcessRole(testIdentity.getMainActorId(), negProcessRole);
        assert !authorizationService.canCallCancel(taskId);

        CaseRole posCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "pos_case_role");
        assignCaseRole(testIdentity.getMainActorId(), posCaseRole);
        assert authorizationService.canCallCancel(taskId);

        CaseRole negCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "neg_case_role");
        assignCaseRole(testIdentity.getMainActorId(), negCaseRole);
        assert !authorizationService.canCallCancel(taskId);

        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        assignAppRole(testIdentity.getMainActorId(), adminAppRole);
        assert authorizationService.canCallCancel(taskId);

        TestHelper.logout();
        assert !authorizationService.canCallCancel(taskId);
    }

    @Test
    public void canCallCancelByGroups() {
        // order of assertions is important!
        User testUser = initializeTestUserWithGroup();

        assert !roleAssignmentRepository.findAllByActorId(testUser.getStringId()).iterator().hasNext();

        assert !authorizationService.canCallCancel(null);

        String taskId = testCase.getTaskStringId("t_cancel");
        String taskIdWithDefault = testCaseWithDefault.getTaskStringId("t_cancel");

        assert !authorizationService.canCallCancel(taskId);
        updateAssigneeOfTask(taskIdWithDefault, sessionManagerService.getActiveActorId());
        assert !authorizationService.canCallCancel(taskIdWithDefault);

        testGroup = updateGroupWithParent(testGroup, groupService.getDefaultGroup().getStringId());

        assert !authorizationService.canCallCancel(taskId);
        assert authorizationService.canCallCancel(taskIdWithDefault);

        updateAssigneeOfTask(taskId, new ObjectId().toString());
        assert !authorizationService.canCallCancel(taskId);

        ProcessRole posProcessRole = processRoleRepository.findByImportId("pos_process_role");
        assignProcessRole(testGroup.getStringId(), posProcessRole);
        assert !authorizationService.canCallCancel(taskId);

        updateAssigneeOfTask(taskId, sessionManagerService.getActiveActorId());
        assert authorizationService.canCallCancel(taskId);

        ProcessRole negProcessRole = processRoleRepository.findByImportId("neg_process_role");
        assignProcessRole(testGroup.getStringId(), negProcessRole);
        assert !authorizationService.canCallCancel(taskId);

        CaseRole posCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "pos_case_role");
        assignCaseRole(testGroup.getStringId(), posCaseRole);
        assert authorizationService.canCallCancel(taskId);

        CaseRole negCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "neg_case_role");
        assignCaseRole(testGroup.getStringId(), negCaseRole);
        assert !authorizationService.canCallCancel(taskId);

        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        assignAppRole(testGroup.getStringId(), adminAppRole);
        assert authorizationService.canCallCancel(taskId);

        TestHelper.logout();
        assert !authorizationService.canCallCancel(taskId);
    }

    @Test
    public void canCallReassign() {
        // order of assertions is important!
        assert !authorizationService.canCallReassign(null);

        String taskId = testCase.getTaskStringId("t_reassign");
        String taskIdWithDefault = testCaseWithDefault.getTaskStringId("t_reassign");

        assert !authorizationService.canCallReassign(taskId);

        updateAssigneeOfTask(taskIdWithDefault, new ObjectId().toString());
        assert authorizationService.canCallReassign(taskIdWithDefault);

        updateAssigneeOfTask(taskId, new ObjectId().toString());
        assert !authorizationService.canCallReassign(taskId);

        ProcessRole posProcessRole = processRoleRepository.findByImportId("pos_process_role");
        assignProcessRole(testIdentity.getMainActorId(), posProcessRole);
        assert authorizationService.canCallReassign(taskId);

        ProcessRole negProcessRole = processRoleRepository.findByImportId("neg_process_role");
        assignProcessRole(testIdentity.getMainActorId(), negProcessRole);
        assert !authorizationService.canCallReassign(taskId);

        CaseRole posCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "pos_case_role");
        assignCaseRole(testIdentity.getMainActorId(), posCaseRole);
        assert authorizationService.canCallReassign(taskId);

        CaseRole negCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "neg_case_role");
        assignCaseRole(testIdentity.getMainActorId(), negCaseRole);
        assert !authorizationService.canCallReassign(taskId);

        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        assignAppRole(testIdentity.getMainActorId(), adminAppRole);
        assert authorizationService.canCallReassign(taskId);

        TestHelper.logout();
        assert !authorizationService.canCallReassign(taskId);
    }

    @Test
    public void canCallReassignByGroups() {
        // order of assertions is important!
        User testUser = initializeTestUserWithGroup();

        assert !roleAssignmentRepository.findAllByActorId(testUser.getStringId()).iterator().hasNext();

        assert !authorizationService.canCallReassign(null);

        String taskId = testCase.getTaskStringId("t_reassign");
        String taskIdWithDefault = testCaseWithDefault.getTaskStringId("t_reassign");

        assert !authorizationService.canCallReassign(taskId);
        updateAssigneeOfTask(taskIdWithDefault, new ObjectId().toString());
        assert !authorizationService.canCallReassign(taskIdWithDefault);

        testGroup = updateGroupWithParent(testGroup, groupService.getDefaultGroup().getStringId());

        assert !authorizationService.canCallReassign(taskId);
        assert authorizationService.canCallReassign(taskIdWithDefault);

        updateAssigneeOfTask(taskId, new ObjectId().toString());
        assert !authorizationService.canCallReassign(taskId);

        ProcessRole posProcessRole = processRoleRepository.findByImportId("pos_process_role");
        assignProcessRole(testGroup.getStringId(), posProcessRole);
        assert authorizationService.canCallReassign(taskId);

        ProcessRole negProcessRole = processRoleRepository.findByImportId("neg_process_role");
        assignProcessRole(testGroup.getStringId(), negProcessRole);
        assert !authorizationService.canCallReassign(taskId);

        CaseRole posCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "pos_case_role");
        assignCaseRole(testGroup.getStringId(), posCaseRole);
        assert authorizationService.canCallReassign(taskId);

        CaseRole negCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "neg_case_role");
        assignCaseRole(testGroup.getStringId(), negCaseRole);
        assert !authorizationService.canCallReassign(taskId);

        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        assignAppRole(testGroup.getStringId(), adminAppRole);
        assert authorizationService.canCallReassign(taskId);

        TestHelper.logout();
        assert !authorizationService.canCallReassign(taskId);
    }

    @Test
    public void canCallFinish() {
        // order of assertions is important!
        assert !authorizationService.canCallFinish(null);

        String taskId = testCase.getTaskStringId("t_finish");
        String taskIdWithDefault = testCaseWithDefault.getTaskStringId("t_finish");

        assert !authorizationService.canCallFinish(taskId);

        updateAssigneeOfTask(taskIdWithDefault, sessionManagerService.getActiveActorId());
        assert authorizationService.canCallFinish(taskIdWithDefault);

        updateAssigneeOfTask(taskId, new ObjectId().toString());
        assert !authorizationService.canCallFinish(taskId);

        ProcessRole posProcessRole = processRoleRepository.findByImportId("pos_process_role");
        assignProcessRole(testIdentity.getMainActorId(), posProcessRole);
        assert !authorizationService.canCallFinish(taskId);

        updateAssigneeOfTask(taskId, sessionManagerService.getActiveActorId());
        assert authorizationService.canCallFinish(taskId);

        ProcessRole negProcessRole = processRoleRepository.findByImportId("neg_process_role");
        assignProcessRole(testIdentity.getMainActorId(), negProcessRole);
        assert !authorizationService.canCallFinish(taskId);

        CaseRole posCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "pos_case_role");
        assignCaseRole(testIdentity.getMainActorId(), posCaseRole);
        assert authorizationService.canCallFinish(taskId);

        CaseRole negCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "neg_case_role");
        assignCaseRole(testIdentity.getMainActorId(), negCaseRole);
        assert !authorizationService.canCallFinish(taskId);

        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        assignAppRole(testIdentity.getMainActorId(), adminAppRole);
        assert authorizationService.canCallFinish(taskId);

        TestHelper.logout();
        assert !authorizationService.canCallFinish(taskId);
    }

    @Test
    public void canCallFinishByGroups() {
        // order of assertions is important!
        User testUser = initializeTestUserWithGroup();

        assert !roleAssignmentRepository.findAllByActorId(testUser.getStringId()).iterator().hasNext();

        assert !authorizationService.canCallFinish(null);

        String taskId = testCase.getTaskStringId("t_finish");
        String taskIdWithDefault = testCaseWithDefault.getTaskStringId("t_finish");

        assert !authorizationService.canCallFinish(taskId);
        updateAssigneeOfTask(taskIdWithDefault, sessionManagerService.getActiveActorId());
        assert !authorizationService.canCallFinish(taskIdWithDefault);

        testGroup = updateGroupWithParent(testGroup, groupService.getDefaultGroup().getStringId());

        assert !authorizationService.canCallFinish(taskId);
        assert authorizationService.canCallFinish(taskIdWithDefault);

        updateAssigneeOfTask(taskId, new ObjectId().toString());
        assert !authorizationService.canCallFinish(taskId);

        ProcessRole posProcessRole = processRoleRepository.findByImportId("pos_process_role");
        assignProcessRole(testGroup.getStringId(), posProcessRole);
        assert !authorizationService.canCallFinish(taskId);

        updateAssigneeOfTask(taskId, sessionManagerService.getActiveActorId());
        assert authorizationService.canCallFinish(taskId);

        ProcessRole negProcessRole = processRoleRepository.findByImportId("neg_process_role");
        assignProcessRole(testGroup.getStringId(), negProcessRole);
        assert !authorizationService.canCallFinish(taskId);

        CaseRole posCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "pos_case_role");
        assignCaseRole(testGroup.getStringId(), posCaseRole);
        assert authorizationService.canCallFinish(taskId);

        CaseRole negCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "neg_case_role");
        assignCaseRole(testGroup.getStringId(), negCaseRole);
        assert !authorizationService.canCallFinish(taskId);

        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        assignAppRole(testGroup.getStringId(), adminAppRole);
        assert authorizationService.canCallFinish(taskId);

        TestHelper.logout();
        assert !authorizationService.canCallFinish(taskId);
    }

    @Test
    public void canCallSetData() {
        // order of assertions is important!
        assert !authorizationService.canCallSetData(null);

        String taskId = testCase.getTaskStringId("t_setdata");

        assert !authorizationService.canCallSetData(taskId);

        updateAssigneeOfTask(taskId, new ObjectId().toString());
        assert !authorizationService.canCallSetData(taskId);

        updateAssigneeOfTask(taskId, sessionManagerService.getActiveActorId());
        assert authorizationService.canCallSetData(taskId);

        updateAssigneeOfTask(taskId, new ObjectId().toString());
        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        assignAppRole(testIdentity.getMainActorId(), adminAppRole);
        assert authorizationService.canCallSetData(taskId);

        updateAssigneeOfTask(taskId, sessionManagerService.getActiveActorId());
        TestHelper.logout();
        assert !authorizationService.canCallSetData(taskId);
    }

    @Test
    public void canCallSetDataByGroups() {
        // order of assertions is important!
        User testUser = initializeTestUserWithGroup();

        assert !roleAssignmentRepository.findAllByActorId(testUser.getStringId()).iterator().hasNext();

        assert !authorizationService.canCallSetData(null);

        String taskId = testCase.getTaskStringId("t_setdata");

        assert !authorizationService.canCallSetData(taskId);

        updateAssigneeOfTask(taskId, new ObjectId().toString());
        assert !authorizationService.canCallSetData(taskId);

        updateAssigneeOfTask(taskId, sessionManagerService.getActiveActorId());
        assert authorizationService.canCallSetData(taskId);

        updateAssigneeOfTask(taskId, new ObjectId().toString());
        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        assignAppRole(testGroup.getStringId(), adminAppRole);
        assert authorizationService.canCallSetData(taskId);

        updateAssigneeOfTask(taskId, sessionManagerService.getActiveActorId());
        TestHelper.logout();
        assert !authorizationService.canCallSetData(taskId);
    }

    @Test
    public void canCallGetData() {
        // order of assertions is important!
        assert !authorizationService.canCallGetData(null);

        String taskId = testCase.getTaskStringId("t_getdata");
        String taskIdWithDefault = testCaseWithDefault.getTaskStringId("t_getdata");

        assert !authorizationService.canCallGetData(taskId);

        assert authorizationService.canCallGetData(taskIdWithDefault);
        updateStateOfTask(taskIdWithDefault, State.DISABLED);
        assert !authorizationService.canCallGetData(taskIdWithDefault);

        ProcessRole posProcessRole = processRoleRepository.findByImportId("pos_process_role");
        assignProcessRole(testIdentity.getMainActorId(), posProcessRole);
        assert authorizationService.canCallGetData(taskId);

        ProcessRole negProcessRole = processRoleRepository.findByImportId("neg_process_role");
        assignProcessRole(testIdentity.getMainActorId(), negProcessRole);
        assert !authorizationService.canCallGetData(taskId);

        CaseRole posCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "pos_case_role");
        assignCaseRole(testIdentity.getMainActorId(), posCaseRole);
        assert authorizationService.canCallGetData(taskId);

        CaseRole negCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "neg_case_role");
        assignCaseRole(testIdentity.getMainActorId(), negCaseRole);
        assert !authorizationService.canCallGetData(taskId);

        updateStateOfTask(taskId, State.DISABLED);

        assert !authorizationService.canCallGetData(taskId);

        posProcessRole = processRoleRepository.findByImportId("pos_getdata_disabled_process_role");
        assignProcessRole(testIdentity.getMainActorId(), posProcessRole);
        assert authorizationService.canCallGetData(taskId);

        negProcessRole = processRoleRepository.findByImportId("neg_getdata_disabled_process_role");
        assignProcessRole(testIdentity.getMainActorId(), negProcessRole);
        assert !authorizationService.canCallGetData(taskId);

        posCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "pos_getdata_disabled_case_role");
        assignCaseRole(testIdentity.getMainActorId(), posCaseRole);
        assert authorizationService.canCallGetData(taskId);

        negCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "neg_getdata_disabled_case_role");
        assignCaseRole(testIdentity.getMainActorId(), negCaseRole);
        assert !authorizationService.canCallGetData(taskId);

        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        assignAppRole(testIdentity.getMainActorId(), adminAppRole);
        assert authorizationService.canCallGetData(taskId);

        TestHelper.logout();
        assert !authorizationService.canCallGetData(taskId);
    }

    @Test
    public void canCallGetDataByGroups() {
        // order of assertions is important!
        User testUser = initializeTestUserWithGroup();
        assert !roleAssignmentRepository.findAllByActorId(testUser.getStringId()).iterator().hasNext();

        assert !authorizationService.canCallGetData(null);

        String taskId = testCase.getTaskStringId("t_getdata");
        String taskIdWithDefault = testCaseWithDefault.getTaskStringId("t_getdata");

        assert !authorizationService.canCallGetData(taskId);
        assert !authorizationService.canCallGetData(taskIdWithDefault);

        testGroup = updateGroupWithParent(testGroup, groupService.getDefaultGroup().getStringId());

        assert !authorizationService.canCallGetData(taskId);
        assert authorizationService.canCallGetData(taskIdWithDefault);

        updateStateOfTask(taskIdWithDefault, State.DISABLED);
        assert !authorizationService.canCallGetData(taskIdWithDefault);

        ProcessRole posProcessRole = processRoleRepository.findByImportId("pos_process_role");
        assignProcessRole(testGroup.getStringId(), posProcessRole);
        assert authorizationService.canCallGetData(taskId);

        ProcessRole negProcessRole = processRoleRepository.findByImportId("neg_process_role");
        assignProcessRole(testGroup.getStringId(), negProcessRole);
        assert !authorizationService.canCallGetData(taskId);

        CaseRole posCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "pos_case_role");
        assignCaseRole(testGroup.getStringId(), posCaseRole);
        assert authorizationService.canCallGetData(taskId);

        CaseRole negCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "neg_case_role");
        assignCaseRole(testGroup.getStringId(), negCaseRole);
        assert !authorizationService.canCallGetData(taskId);

        updateStateOfTask(taskId, State.DISABLED);

        assert !authorizationService.canCallGetData(taskId);

        posProcessRole = processRoleRepository.findByImportId("pos_getdata_disabled_process_role");
        assignProcessRole(testGroup.getStringId(), posProcessRole);
        assert authorizationService.canCallGetData(taskId);

        negProcessRole = processRoleRepository.findByImportId("neg_getdata_disabled_process_role");
        assignProcessRole(testGroup.getStringId(), negProcessRole);
        assert !authorizationService.canCallGetData(taskId);

        posCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "pos_getdata_disabled_case_role");
        assignCaseRole(testGroup.getStringId(), posCaseRole);
        assert authorizationService.canCallGetData(taskId);

        negCaseRole = caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "neg_getdata_disabled_case_role");
        assignCaseRole(testGroup.getStringId(), negCaseRole);
        assert !authorizationService.canCallGetData(taskId);

        ApplicationRole adminAppRole = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        assignAppRole(testGroup.getStringId(), adminAppRole);
        assert authorizationService.canCallGetData(taskId);

        TestHelper.logout();
        assert !authorizationService.canCallGetData(taskId);
    }

    @Test
    public void canCallGetDataDisabled() {
        String taskId = testCaseWithDefault.getTaskStringId("t_getdata_disabled");

        assert authorizationService.canCallGetData(taskId);
        updateStateOfTask(taskId, State.ENABLED);
        assert !authorizationService.canCallGetData(taskId);
    }

    @Test
    public void canCallGetDataDisabledByGroups() {
        User testUser = initializeTestUserWithGroup();
        assert !roleAssignmentRepository.findAllByActorId(testUser.getStringId()).iterator().hasNext();

        String taskId = testCaseWithDefault.getTaskStringId("t_getdata_disabled");

        assert !authorizationService.canCallGetData(taskId);
        testGroup = updateGroupWithParent(testGroup, groupService.getDefaultGroup().getStringId());
        assert authorizationService.canCallGetData(taskId);

        updateStateOfTask(taskId, State.ENABLED);
        assert !authorizationService.canCallGetData(taskId);
    }

    private void updateAssigneeOfTask(String taskId, String newAssigneeId) {
        Task task = taskService.findOne(taskId);
        task.setAssigneeId(newAssigneeId);
        taskService.save(task);
    }

    private void updateStateOfTask(String taskId, State newState) {
        Task task = taskService.findOne(taskId);
        task.setState(newState);
        taskService.save(task);
    }

    private void assignProcessRole(String actorId, ProcessRole role) {
        RoleAssignment assignment = new ProcessRoleAssignment();
        assignment.setActorId(actorId);
        assignment.setRoleId(role.getStringId());
        roleAssignmentRepository.save(assignment);
    }

    private void assignCaseRole(String actorId, CaseRole role) {
        CaseRoleAssignment assignment = new CaseRoleAssignment();
        assignment.setActorId(actorId);
        assignment.setRoleId(role.getStringId());
        assignment.setCaseId(testCase.getStringId());
        roleAssignmentRepository.save(assignment);
    }

    private void assignAppRole(String actorId, ApplicationRole role) {
        ApplicationRoleAssignment assignment = new ApplicationRoleAssignment();
        assignment.setActorId(actorId);
        assignment.setRoleId(role.getStringId());
        roleAssignmentRepository.save(assignment);
    }

    private Group updateGroupWithParent(Group group, String parentGroupId) {
        return new Group(dataService.setData(group.getCase(), GroupParams.with()
                .parentGroupId(CaseField.withValue(List.of(parentGroupId)))
                .build()
                .toDataSet(), null).getCase());
    }

    private User updateUserMembership(User user, Set<String> groupIds) {
        return new User(dataService.setData(user.getCase(), UserParams.with()
                .groupIds(CaseField.withValue(new ArrayList<>(groupIds)))
                .build()
                .toDataSet(), null).getCase());
    }

    private User initializeTestUserWithGroup() {
        Optional<User> testUserOpt = userService.findById(testIdentity.getMainActorId());
        assert testUserOpt.isPresent();
        testGroup = createGroup("test group");
        return updateUserMembership(testUserOpt.get(), Set.of(testGroup.getStringId()));
    }

    private Group createGroup(String name) {
        Case groupCase = workflowService.createCaseByIdentifier(GroupConstants.PROCESS_IDENTIFIER, name, "", null).getCase();
        return new Group(dataService.setData(groupCase, GroupParams.with()
                .name(new TextField(name))
                .build()
                .toDataSet(), null).getCase());
    }
}
