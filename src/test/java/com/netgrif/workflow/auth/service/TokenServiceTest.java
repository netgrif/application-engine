package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.UnactivatedUser;
import com.netgrif.workflow.auth.domain.repositories.UnactivatedUserRepository;
import com.netgrif.workflow.auth.service.interfaces.IUnactivatedUserService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

@RunWith(SpringRunner.class)
@ActiveProfiles({"test"})
@SpringBootTest
public class TokenServiceTest {

    @Autowired
    IUnactivatedUserService service;

    @Autowired
    UnactivatedUserRepository repository;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUp() {
        repository.deleteAll();
    }

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void removeExpired() throws Exception {
        UnactivatedUser expired = new UnactivatedUser();
        expired.setEmail("test@test.com");
        expired.setToken("token");
        expired.setExpirationDate(LocalDateTime.now().minusDays(10));
        repository.save(expired);

        UnactivatedUser expired2 = new UnactivatedUser();
        expired2.setEmail("test2@test.com");
        expired2.setToken("token2");
        //expired2.setExpirationDate(LocalDateTime.now().minusDays(10));
        repository.save(expired);

        service.removeExpired();

        assert repository.findAll().size() == 1;
    }

    @Test
    public void authorizeToken() throws Exception {
        UnactivatedUser expired = new UnactivatedUser();
        expired.setEmail("test3@test.com");
        expired.setToken("token3");
        repository.save(expired);

        boolean authorized = service.authorizeToken("test3@test.com", "token3");
        UnactivatedUser token = repository.findByEmail("test3@test.com");

        assertTokenRemoved(authorized, token);
    }

    private void assertTokenRemoved(boolean authorized, UnactivatedUser token) {
        assert authorized;
        assert token != null;
    }
}