package com.netgrif.workflow.auth.service;


import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.repositories.UserRepository;
import com.netgrif.workflow.auth.service.interfaces.IRegistrationService;
import com.netgrif.workflow.auth.web.requestbodies.NewUserRequest;
import com.netgrif.workflow.auth.web.requestbodies.RegistrationRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles({"test"})
@SpringBootTest
public class RegistrationServiceTest {

    @Autowired
    IRegistrationService service;

    @Autowired
    UserRepository repository;


    @Before
    public void setUp() {
        repository.deleteAll();
    }

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    @WithMockUser(username = "myUser", roles = { "myAuthority" })
    public void testRegisterUser() throws InvalidUserTokenException {
        NewUserRequest request = new NewUserRequest();
        request.email = "test@test.com";
        User user = service.createNewUser(request);

        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.token = service.encodeToken(user.getEmail(), user.getToken());
        registrationRequest.password = "password";
        registrationRequest.name = "Stefan";
        registrationRequest.surname = "Renczes";

        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        User registered = service.registerUser(registrationRequest);

        assert registered != null;
    }

}
