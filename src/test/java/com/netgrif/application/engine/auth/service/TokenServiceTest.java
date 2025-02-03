package com.netgrif.application.engine.auth.service;

import com.netgrif.adapter.auth.service.UserService;
import com.netgrif.core.auth.domain.User;
import com.netgrif.core.auth.domain.enums.UserState;
import com.netgrif.application.engine.auth.service.interfaces.IRegistrationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test"})
@SpringBootTest
public class TokenServiceTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    @Autowired
    IRegistrationService service;
    @Autowired
    UserService userService;

    @BeforeEach
    public void setUp() {
        userService.deleteAllUsers();
    }

    @AfterEach
    public void cleanUp() {
        userService.deleteAllUsers();
    }

    @Test
    public void removeExpired() throws Exception {
        User expired = new User();
        expired.setUsername("test1@test.com");
        expired.setEmail("test1@test.com");
        expired.setPassword("password");
        expired.setToken("token");
        expired.setExpirationDate(LocalDateTime.now().minusDays(10));
        expired.setState(UserState.INACTIVE);
        userService.saveUser(expired, null);

        User expired2 = new User();
        expired2.setUsername("test2@test.com");
        expired2.setEmail("test2@test.com");
        expired2.setPassword("password");
        expired2.setToken("token2");
        expired2.setState(UserState.INACTIVE);
        userService.saveUser(expired2, null);

        service.removeExpiredUsers();

        assert userService.findAllUsers(null).size() == 1;
    }

    @Test
    public void authorizeToken() throws Exception {
        User expired = new User();
        expired.setToken("token3");
        expired.setUsername("test3@test.com");
        expired.setEmail("test3@test.com");
        expired.setPassword("password");
        expired.setExpirationDate(LocalDateTime.now().minusDays(10));
        expired.setState(UserState.INACTIVE);
        userService.saveUser(expired, null);

        boolean authorized = service.verifyToken(service.encodeToken("test3@test.com", "token3"));
        User token = (User) userService.findByEmail("test3@test.com", null);

        assertTokenRemoved(authorized, token);
    }

    private void assertTokenRemoved(boolean authorized, User token) {
        assert authorized;
        assert token != null;
    }
}