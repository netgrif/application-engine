package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.domain.ApplicationRoleAssignment;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.domain.repositories.RoleAssignmentRepository;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.startup.ApplicationRoleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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

    private Identity testIdentity;

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
}
