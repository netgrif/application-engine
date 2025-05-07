package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.domain.ApplicationRoleAssignment;
import com.netgrif.application.engine.authorization.domain.Group;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.constants.GroupConstants;
import com.netgrif.application.engine.authorization.domain.params.GroupParams;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
import com.netgrif.application.engine.authorization.domain.repositories.RoleAssignmentRepository;
import com.netgrif.application.engine.authorization.service.interfaces.IUserService;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.startup.ApplicationRoleRunner;
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

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class ApplicationAuthorizationServiceTest {

    @Autowired
    private ApplicationAuthorizationService authorizationService;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IIdentityService identityService;

    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;

    @Autowired
    private ApplicationRoleRunner applicationRoleRunner;

    @Autowired
    private IUserService userService;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IDataService dataService;

    private Identity testIdentity;

    private Group testGroup;

    @BeforeEach
    public void beforeEach() {
        testHelper.truncateDbs();

        testIdentity = identityService.createWithDefaultUser(IdentityParams.with()
                .username(new TextField("username"))
                .password(new TextField("password"))
                .firstname(new TextField("firstname"))
                .lastname(new TextField("lastname"))
                .build());

        TestHelper.login(testIdentity);
    }

    @Test
    public void hasApplicationRole() {
        assert !authorizationService.hasApplicationRole(null);
        assert !authorizationService.hasApplicationRole("nonExistingRoleName");
        assert !authorizationService.hasApplicationRole(ApplicationRoleRunner.ADMIN_APP_ROLE);

        Role role = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        ApplicationRoleAssignment assignment = new ApplicationRoleAssignment();
        assignment.setActorId(testIdentity.getMainActorId());
        assignment.setRoleId(role.getStringId());
        roleAssignmentRepository.save(assignment);

        assert authorizationService.hasApplicationRole(ApplicationRoleRunner.ADMIN_APP_ROLE);

        TestHelper.logout();
        assert !authorizationService.hasApplicationRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
    }

    @Test
    public void hasApplicationRoleByGroups() {
        User testUser = initializeTestUserWithGroup();
        assert !roleAssignmentRepository.findAllByActorId(testUser.getStringId()).iterator().hasNext();

        assert !authorizationService.hasApplicationRole(null);
        assert !authorizationService.hasApplicationRole("nonExistingRoleName");
        assert !authorizationService.hasApplicationRole(ApplicationRoleRunner.ADMIN_APP_ROLE);

        Role role = applicationRoleRunner.getAppRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
        ApplicationRoleAssignment assignment = new ApplicationRoleAssignment();
        assignment.setActorId(testGroup.getStringId());
        assignment.setRoleId(role.getStringId());
        roleAssignmentRepository.save(assignment);

        assert authorizationService.hasApplicationRole(ApplicationRoleRunner.ADMIN_APP_ROLE);

        TestHelper.logout();
        assert !authorizationService.hasApplicationRole(ApplicationRoleRunner.ADMIN_APP_ROLE);
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
