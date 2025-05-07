package com.netgrif.application.engine.elastic;

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
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.ApplicationRoleRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.SuperCreator;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.netgrif.application.engine.petrinet.domain.Process;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class ElasticCaseSearchPermissionTest {

    @Autowired
    private IElasticCaseService elasticCaseService;

    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @Autowired
    private CaseRoleRepository caseRoleRepository;

    @Autowired
    private ApplicationRoleRunner applicationRoleRunner;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private SuperCreator superCreator;

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

    private CaseSearchRequest request;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
        createIdentityWithActor();
    }

    /**
     * todo javadoc
     * */
    @Test
    public void testViewPermissionsOrdered() throws InterruptedException, IOException, MissingPetriNetMetaDataException {
        createTestCase("case_authorization_test");
        buildSearchRequest();
        Thread.sleep(2000);

        assertWithoutRole();
        String actorId = testIdentity.getMainActorId();
        assertWithAddedPosProcessRole(actorId);
        assertWithAddedNegProcessRole(actorId);
        assertWithAddedPosCaseRole(actorId);
        assertWithAddedNegCaseRole(actorId);
        assertWithAddedAdminAppRole(actorId);
    }

    /**
     * todo javadoc
     * */
    @Test
    public void testViewPermissionsByGroupsOrdered() throws InterruptedException, IOException, MissingPetriNetMetaDataException {
        createTestCase("case_authorization_test");
        buildSearchRequest();
        initializeTestUserWithGroup();
        Thread.sleep(2000);

        assertWithoutRole();
        String actorId = testGroup.getStringId();
        assertWithAddedPosProcessRole(actorId);
        assertWithAddedNegProcessRole(actorId);
        assertWithAddedPosCaseRole(actorId);
        assertWithAddedNegCaseRole(actorId);
        assertWithAddedAdminAppRole(actorId);
    }

    /**
     * todo javadoc
     * */
    @Test
    public void testViewWithDefaultPermission() throws IOException, MissingPetriNetMetaDataException, InterruptedException {
        createTestCase("case_authorization_default_test");
        buildSearchRequest();
        Thread.sleep(2000);

        Page<Case> pagedResult = doSearch();

        assert pagedResult.hasContent();
        assert pagedResult.getTotalElements() == 1;
    }

    /**
     * todo javadoc
     * */
    @Test
    public void testViewWithDefaultPermissionByGroups() throws IOException, MissingPetriNetMetaDataException, InterruptedException {
        createTestCase("case_authorization_default_test");
        buildSearchRequest();
        initializeTestUserWithGroup();
        Thread.sleep(2000);

        Page<Case> pagedResult = doSearch();
        assert !pagedResult.hasContent();

        testGroup = updateGroupWithParent(testGroup, groupService.getDefaultGroup().getStringId());

        pagedResult = doSearch();
        assert pagedResult.hasContent();
        assert pagedResult.getTotalElements() == 1;
    }

    private void createIdentityWithActor() {
        testIdentity = importHelper.createIdentity(IdentityParams.with()
                .username(new TextField("username"))
                .password(new TextField("password"))
                .firstname(new TextField("firstname"))
                .lastname(new TextField("lastname"))
                .build(), new ArrayList<>());
    }

    private void createTestCase(String identifier) throws IOException, MissingPetriNetMetaDataException {
        Process process = petriNetService.importPetriNet(new FileInputStream(String.format("src/test/resources/petriNets/%s.xml", identifier)),
                VersionType.MAJOR, superCreator.getLoggedSuper().getActiveActorId()).getNet();
        TestHelper.login(superCreator.getSuperIdentity());
        testCase = importHelper.createCase("Case permissions", process);
        TestHelper.logout();
    }

    private void buildSearchRequest() {
        request = new CaseSearchRequest(Map.of("stringId", List.of(testCase.getStringId())));
    }

    /**
     * todo javadoc
     * */
    private void assertWithoutRole() {
        Page<Case> pagedResult = doSearch();
        assert !pagedResult.hasContent();
    }

    /**
     * todo javadoc
     * */
    private void assertWithAddedPosProcessRole(String actorId) {
        assignProcessRole(actorId, processRoleRepository.findByImportId("pos_process_role"));

        Page<Case> pagedResult = doSearch();
        assert pagedResult.hasContent();
        assert pagedResult.getTotalElements() == 1;
    }

    /**
     * todo javadoc
     * */
    private void assertWithAddedNegProcessRole(String actorId) {
        assignProcessRole(actorId, processRoleRepository.findByImportId("neg_process_role"));

        Page<Case> pagedResult = doSearch();
        assert !pagedResult.hasContent();
    }

    /**
     * todo javadoc
     * */
    private void assertWithAddedPosCaseRole(String actorId) {
        assignCaseRole(actorId, caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "pos_case_role"));

        Page<Case> pagedResult = doSearch();
        assert pagedResult.hasContent();
        assert pagedResult.getTotalElements() == 1;
    }

    /**
     * todo javadoc
     * */
    private void assertWithAddedNegCaseRole(String actorId) {
        assignCaseRole(actorId, caseRoleRepository.findByCaseIdAndImportId(testCase.getStringId(), "neg_case_role"));

        Page<Case> pagedResult = doSearch();
        assert !pagedResult.hasContent();
    }

    /**
     * todo javadoc
     * */
    private void assertWithAddedAdminAppRole(String actorId) {
        assignAppRole(actorId, applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE));

        Page<Case> pagedResult = doSearch();
        assert pagedResult.hasContent();
        assert pagedResult.getTotalElements() == 1;
    }

    private Page<Case> doSearch() {
        return elasticCaseService.search(List.of(request), testIdentity.toSession(), PageRequest.of(0, 2),
                Locale.getDefault(), false);
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


}
