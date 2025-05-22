package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.domain.*;
import com.netgrif.application.engine.authorization.domain.constants.GroupConstants;
import com.netgrif.application.engine.authorization.domain.params.GroupParams;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
import com.netgrif.application.engine.authorization.domain.repositories.CaseRoleRepository;
import com.netgrif.application.engine.authorization.domain.repositories.ProcessRoleRepository;
import com.netgrif.application.engine.authorization.domain.repositories.RoleAssignmentRepository;
import com.netgrif.application.engine.authorization.service.interfaces.IGroupService;
import com.netgrif.application.engine.authorization.service.interfaces.IUserService;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.ApplicationRoleRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.netgrif.application.engine.petrinet.domain.Process;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class CaseAuthorizationServiceTest {

    @Autowired
    private CaseAuthorizationService authorizationService;

    @Autowired
    private IIdentityService identityService;

    @Autowired
    private IPetriNetService petriNetService;

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

    private Process testProcess;

    private Process testProcessWithDefault;

    @BeforeEach
    public void beforeEach() throws IOException, MissingPetriNetMetaDataException {
        testHelper.truncateDbs();

        testIdentity = identityService.createWithDefaultUser(IdentityParams.with()
                        .username(new TextField("username"))
                        .password(new TextField("password"))
                        .firstname(new TextField("firstname"))
                        .lastname(new TextField("lastname"))
                .build());

        testProcess = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/case_authorization_test.xml"),
                VersionType.MAJOR, userService.getSystemUser().getStringId()).getNet();

        testProcessWithDefault = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/case_authorization_default_test.xml"),
                VersionType.MAJOR, userService.getSystemUser().getStringId()).getNet();

        TestHelper.login(testIdentity);
    }

    @Test
    public void canCallCreate() {
        assert !authorizationService.canCallCreate(null);
        assert !authorizationService.canCallCreate("wrong id");

        // order of assertions is important!
        assert !authorizationService.canCallCreate(null);
        assert authorizationService.canCallCreate(testProcessWithDefault.getStringId());
        assert !authorizationService.canCallCreate(testProcess.getStringId());

        assignProcessRole(testIdentity.getMainActorId(), processRoleRepository.findByImportId("pos_process_role"));
        assert authorizationService.canCallCreate(testProcess.getStringId());

        assignProcessRole(testIdentity.getMainActorId(), processRoleRepository.findByImportId("neg_process_role"));
        assert !authorizationService.canCallCreate(testProcess.getStringId());

        assignAppRole(testIdentity.getMainActorId(), applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE));
        assert authorizationService.canCallCreate(testProcess.getStringId());

        TestHelper.logout();
        assert !authorizationService.canCallCreate(testProcess.getStringId());
    }

    @Test
    public void canCallCreateByGroups() {
        // order of assertions is important!
        User testUser = initializeTestUserWithGroup();

        assert !roleAssignmentRepository.findAllByActorId(testUser.getStringId()).iterator().hasNext();

        assert !authorizationService.canCallCreate(null);
        assert !authorizationService.canCallCreate(testProcessWithDefault.getStringId());
        assert !authorizationService.canCallCreate(testProcess.getStringId());

        testGroup = updateGroupWithParent(testGroup, groupService.getDefaultGroup().getStringId());
        assert authorizationService.canCallCreate(testProcessWithDefault.getStringId());
        assert !authorizationService.canCallCreate(testProcess.getStringId());

        assignProcessRole(testGroup.getStringId(), processRoleRepository.findByImportId("pos_process_role"));
        assert authorizationService.canCallCreate(testProcess.getStringId());

        assignProcessRole(testGroup.getStringId(), processRoleRepository.findByImportId("neg_process_role"));
        assert !authorizationService.canCallCreate(testProcess.getStringId());

        assignAppRole(testGroup.getStringId(), applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE));
        assert authorizationService.canCallCreate(testProcess.getStringId());

        TestHelper.logout();
        assert !authorizationService.canCallCreate(testProcess.getStringId());
    }

    @Test
    public void canCallDelete() {
        assert !authorizationService.canCallDelete(null);
        assert !authorizationService.canCallDelete("wrong id");

        // order of assertions is important!
        Case testCase = importHelper.createCase("test", testProcess);
        Case testCaseWithDefault = importHelper.createCase("test with default role", testProcessWithDefault);

        assert !authorizationService.canCallDelete(testCase.getStringId());
        assert authorizationService.canCallDelete(testCaseWithDefault.getStringId());

        assignProcessRole(testIdentity.getMainActorId(), processRoleRepository.findByImportId("pos_process_role"));
        assert authorizationService.canCallDelete(testCase.getStringId());

        assignProcessRole(testIdentity.getMainActorId(), processRoleRepository.findByImportId("neg_process_role"));
        assert !authorizationService.canCallDelete(testCase.getStringId());

        assignCaseRole(testIdentity.getMainActorId(), caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(),
                        "pos_case_role"), testCase);
        assert authorizationService.canCallDelete(testCase.getStringId());

        assignCaseRole(testIdentity.getMainActorId(), caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(),
                        "neg_case_role"), testCase);
        assert !authorizationService.canCallDelete(testCase.getStringId());

        assignAppRole(testIdentity.getMainActorId(), applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE));
        assert authorizationService.canCallDelete(testCase.getStringId());

        TestHelper.logout();
        assert !authorizationService.canCallDelete(testCase.getStringId());
    }

    @Test
    public void canCallDeleteByGroups() {
        // order of assertions is important!
        Case testCase = importHelper.createCase("test", testProcess);
        Case testCaseWithDefault = importHelper.createCase("test with default role", testProcessWithDefault);

        User testUser = initializeTestUserWithGroup();

        assert !roleAssignmentRepository.findAllByActorId(testUser.getStringId()).iterator().hasNext();

        assert !authorizationService.canCallDelete(testCase.getStringId());
        assert !authorizationService.canCallDelete(testCaseWithDefault.getStringId());

        testGroup = updateGroupWithParent(testGroup, groupService.getDefaultGroup().getStringId());
        assert !authorizationService.canCallDelete(testCase.getStringId());
        assert authorizationService.canCallDelete(testCaseWithDefault.getStringId());

        assignProcessRole(testGroup.getStringId(), processRoleRepository.findByImportId("pos_process_role"));
        assert authorizationService.canCallDelete(testCase.getStringId());

        assignProcessRole(testGroup.getStringId(), processRoleRepository.findByImportId("neg_process_role"));
        assert !authorizationService.canCallDelete(testCase.getStringId());

        assignCaseRole(testGroup.getStringId(), caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(),
                        "pos_case_role"), testCase);
        assert authorizationService.canCallDelete(testCase.getStringId());

        assignCaseRole(testGroup.getStringId(), caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(),
                        "neg_case_role"), testCase);
        assert !authorizationService.canCallDelete(testCase.getStringId());

        assignAppRole(testGroup.getStringId(), applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE));
        assert authorizationService.canCallDelete(testCase.getStringId());

        TestHelper.logout();
        assert !authorizationService.canCallDelete(testCase.getStringId());
    }

    @Test
    public void canCallView() {
        // order of assertions is important!
        assert !authorizationService.canView(null);
        assert !authorizationService.canView("wrong id");

        Case testCase = importHelper.createCase("test", testProcess);
        Case testCaseWithDefault = importHelper.createCase("test with default role", testProcessWithDefault);

        assert !authorizationService.canView(testCase.getStringId());
        assert authorizationService.canView(testCaseWithDefault.getStringId());

        assignProcessRole(testIdentity.getMainActorId(), processRoleRepository.findByImportId("pos_process_role"));
        assert authorizationService.canView(testCase.getStringId());

        assignProcessRole(testIdentity.getMainActorId(), processRoleRepository.findByImportId("neg_process_role"));
        assert !authorizationService.canView(testCase.getStringId());

        assignCaseRole(testIdentity.getMainActorId(), caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(),
                "pos_case_role"), testCase);
        assert authorizationService.canView(testCase.getStringId());

        assignCaseRole(testIdentity.getMainActorId(), caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(),
                "neg_case_role"), testCase);
        assert !authorizationService.canView(testCase.getStringId());

        assignAppRole(testIdentity.getMainActorId(), applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE));
        assert authorizationService.canView(testCase.getStringId());

        TestHelper.logout();
        assert !authorizationService.canView(testCase.getStringId());
    }

    @Test
    public void canCallViewByGroups() {
        // order of assertions is important!
        Case testCase = importHelper.createCase("test", testProcess);
        Case testCaseWithDefault = importHelper.createCase("test with default role", testProcessWithDefault);

        User testUser = initializeTestUserWithGroup();

        assert !authorizationService.canView(testCase.getStringId());
        assert !authorizationService.canView(testCaseWithDefault.getStringId());

        testGroup = updateGroupWithParent(testGroup, groupService.getDefaultGroup().getStringId());
        assert !authorizationService.canView(testCase.getStringId());
        assert authorizationService.canView(testCaseWithDefault.getStringId());

        assignProcessRole(testGroup.getStringId(), processRoleRepository.findByImportId("pos_process_role"));
        assert authorizationService.canView(testCase.getStringId());

        assignProcessRole(testGroup.getStringId(), processRoleRepository.findByImportId("neg_process_role"));
        assert !authorizationService.canView(testCase.getStringId());

        assignCaseRole(testGroup.getStringId(), caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(),
                "pos_case_role"), testCase);
        assert authorizationService.canView(testCase.getStringId());

        assignCaseRole(testGroup.getStringId(), caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(),
                "neg_case_role"), testCase);
        assert !authorizationService.canView(testCase.getStringId());

        assignAppRole(testGroup.getStringId(), applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE));
        assert authorizationService.canView(testCase.getStringId());

        TestHelper.logout();
        assert !authorizationService.canView(testCase.getStringId());
    }

    @Test
    void testGroupPermissionLoopBreakdown() {
        Group testGroup = createGroup("test group");
        // forbidden reference loop: testGroup -> defaultGroup -> testGroup -> ...
        updateGroupWithParent(groupService.getDefaultGroup(), testGroup.getStringId());
        updateGroupWithParent(testGroup, groupService.getDefaultGroup().getStringId());

        Optional<User> testUserOpt = userService.findById(testIdentity.getMainActorId());
        assert testUserOpt.isPresent();
        // to make it harder, user is a member of both groups
        updateUserMembership(testUserOpt.get(), Set.of(testGroup.getStringId(), groupService.getDefaultGroup().getStringId()));

        assert authorizationService.canCallCreate(testProcessWithDefault.getStringId());

        Case testCaseWithDefault = importHelper.createCase("test with default role", testProcessWithDefault);
        assert authorizationService.canCallDelete(testCaseWithDefault.getStringId());
    }

    private void assignProcessRole(String actorId, ProcessRole role) {
        RoleAssignment assignment = new ProcessRoleAssignment();
        assignment.setActorId(actorId);
        assignment.setRoleId(role.getStringId());
        roleAssignmentRepository.save(assignment);
    }

    private void assignCaseRole(String actorId, CaseRole role, Case testCase) {
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
