package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class UserServiceTest {

    @Autowired
    private IUserService service;

    @Autowired
    private TestHelper testHelper;

    @BeforeEach
    void before() {
        testHelper.truncateDbs();
    }

    @Test
    public void shouldUserExist() {
        IUser user = service.findByEmail("super@netgrif.com", true);
        assertNotNull(user);
        boolean userExists = service.existsById(user.getStringId());
        assertTrue(userExists);
    }

}
