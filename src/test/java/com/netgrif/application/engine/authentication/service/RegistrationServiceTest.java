package com.netgrif.application.engine.authentication.service;


import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.service.interfaces.IRegistrationService;
import com.netgrif.application.engine.authentication.web.requestbodies.NewIdentityRequest;
import com.netgrif.application.engine.authentication.web.requestbodies.RegistrationRequest;
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
    private IRegistrationService service;

    @Autowired
    private TestHelper testHelper;

    @BeforeEach
    void before() {
        testHelper.truncateDbs();
    }

    @Test
//    @WithMockUser(username = "myUser", roles = { "myAuthority" })
    public void testRegisterIdentity() throws InvalidIdentityTokenException {
        NewIdentityRequest request = new NewIdentityRequest();
        request.email = "test@test.com";

        Identity identity = service.createNewIdentity(request);

        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.token = service.encodeToken(identity.getUsername(), identity.getRegistrationToken());
        registrationRequest.password = "password";
        registrationRequest.firstname = "Identity";
        registrationRequest.lastname = "Test";

        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        Identity registered = service.registerIdentity(registrationRequest);

        assert registered != null;
    }

    // todo: release/8.0.0 implement more tests

}
