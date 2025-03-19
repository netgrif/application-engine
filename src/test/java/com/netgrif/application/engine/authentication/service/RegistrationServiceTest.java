package com.netgrif.application.engine.authentication.service;


import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authentication.domain.RegisteredUser;
import com.netgrif.application.engine.authentication.domain.User;
import com.netgrif.application.engine.authentication.domain.repositories.UserRepository;
import com.netgrif.application.engine.authentication.service.interfaces.IRegistrationService;
import com.netgrif.application.engine.authentication.web.requestbodies.NewIdentityRequest;
import com.netgrif.application.engine.authentication.web.requestbodies.RegistrationRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test"})
@SpringBootTest
public class RegistrationServiceTest {

    @Autowired
    IRegistrationService service;

    @Autowired
    UserRepository repository;

    @Autowired
    private TestHelper testHelper;

    @BeforeEach
    void before() {
        testHelper.truncateDbs();
    }


    @AfterEach
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
//    @WithMockUser(username = "myUser", roles = { "myAuthority" })
    public void testRegisterIdentity() throws InvalidIdentityTokenException {
        NewIdentityRequest request = new NewIdentityRequest();
        request.email = "test@test.com";

        RegisteredUser user = service.createNewIdentity(request);

        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.token = service.encodeToken(user.getEmail(), user.getToken());
        registrationRequest.password = "password";
        registrationRequest.name = "User";
        registrationRequest.surname = "Test";

        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        User registered = (User) service.registerIdentity(registrationRequest);

        assert registered != null;
    }

}
