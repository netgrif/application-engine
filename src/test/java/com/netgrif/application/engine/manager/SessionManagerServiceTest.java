package com.netgrif.application.engine.manager;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class SessionManagerServiceTest {

    @Autowired
    private ISessionManagerService managerService;

    @Autowired
    private TestHelper testHelper;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
    }

    @Test
    void getAllLoggedUsersTest() {
        Collection<LoggedUser> user = managerService.getAllLoggedUsers();
        assert user.size() == 0;
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
