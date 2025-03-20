package com.netgrif.application.engine.authentication.service;

import com.netgrif.application.engine.authentication.domain.IdentityState;
import com.netgrif.application.engine.authentication.domain.repositories.UserRepository;
import com.netgrif.application.engine.authentication.service.interfaces.IRegistrationService;
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
    UserRepository repository;

    @BeforeEach
    public void setUp() {
        repository.deleteAll();
    }

    @AfterEach
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void removeExpired() throws Exception {
        User expired = new User("test@test.com", null, User.UNKNOWN, User.UNKNOWN);
        expired.setToken("token");
        expired.setExpirationDate(LocalDateTime.now().minusDays(10));
        expired.setState(IdentityState.INVITED);
        repository.save(expired);

        User expired2 = new User("test2@test.com", null, User.UNKNOWN, User.UNKNOWN);
        expired2.setToken("token2");
        expired2.setState(IdentityState.INVITED);
        repository.save(expired2);

        service.removeExpiredIdentities();

        assert repository.findAll().size() == 1;
    }

    @Test
    public void authorizeToken() throws Exception {
        User expired = new User("test3@test.com", null, User.UNKNOWN, User.UNKNOWN);
        expired.setToken("token3");
        expired.setExpirationDate(LocalDateTime.now().plusMinutes(10));
        expired.setState(IdentityState.INVITED);
        repository.save(expired);

        boolean authorized = service.verifyToken(service.encodeToken("test3@test.com", "token3"));
        User token = repository.findByEmail("test3@test.com");

        assertTokenRemoved(authorized, token);
    }

    private void assertTokenRemoved(boolean authorized, User token) {
        assert authorized;
        assert token != null;
    }
}