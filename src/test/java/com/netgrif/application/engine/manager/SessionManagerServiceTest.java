package com.netgrif.application.engine.manager;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.domain.constants.SystemIdentityConstants;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
class SessionManagerServiceTest {

    @Autowired
    private ISessionManagerService managerService;

    @Autowired
    private TestHelper testHelper;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
    }

    @Test
    void getLoggedIdentity() {
        assert managerService.getLoggedIdentity() == null;

        LoggedIdentity loggedIdentity = login();

        LoggedIdentity receivedLoggedIdentity = managerService.getLoggedIdentity();
        assert receivedLoggedIdentity != null;
        assert receivedLoggedIdentity.getUsername().equals(loggedIdentity.getUsername());
        assert receivedLoggedIdentity.getPassword().equals(loggedIdentity.getPassword());
        assert receivedLoggedIdentity.getFullName().equals(loggedIdentity.getFullName());
        assert receivedLoggedIdentity.getIdentityId().equals(loggedIdentity.getIdentityId());
        assert receivedLoggedIdentity.getActiveActorId().equals(loggedIdentity.getActiveActorId());
    }

    @Test
    void getLoggedSystemIdentity() {
        LoggedIdentity receivedLoggedIdentity = managerService.getLoggedSystemIdentity();

        assert receivedLoggedIdentity != null;
        assert receivedLoggedIdentity.getUsername().equals(SystemIdentityConstants.USERNAME);
        assert receivedLoggedIdentity.getFullName().equals(String.join(" ", SystemIdentityConstants.FIRSTNAME,
                SystemIdentityConstants.LASTNAME));
        assert ObjectId.isValid(receivedLoggedIdentity.getIdentityId());
        assert ObjectId.isValid(receivedLoggedIdentity.getActiveActorId());
        assert !receivedLoggedIdentity.getIdentityId().equals(receivedLoggedIdentity.getActiveActorId());
    }

    @Test
    void getActiveActorId() {
        assert managerService.getActiveActorId() == null;

        LoggedIdentity loggedIdentity = login();

        assert managerService.getActiveActorId().equals(loggedIdentity.getActiveActorId());
    }



    private LoggedIdentity login() {
        LoggedIdentity loggedIdentity = LoggedIdentity.with()
                .username("username")
                .password("password")
                .fullName("fullName")
                .identityId("identityId")
                .activeActorId("activeActorId")
                .build();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loggedIdentity,
                loggedIdentity.getPassword(), loggedIdentity.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);

        return loggedIdentity;
    }

    @Test
    void getAllLoggedIdentitiesTest() {
        assert managerService.getAllLoggedIdentities() != null;
    }

    @Test
    void logoutSessionByUsernameTest() {
        managerService.logoutSessionByUsername("test@netgrif.com");
    }

    @Test
    void logoutAllSessionTest() {
        managerService.logoutAllSession();
    }

}
