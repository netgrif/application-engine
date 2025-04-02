package com.netgrif.application.engine.authentication.service;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.IdentityState;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authentication.service.interfaces.IRegistrationService;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.petrinet.domain.dataset.DateTimeField;
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.startup.ImportHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test"})
@SpringBootTest
public class TokenServiceTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Autowired
    private IRegistrationService service;

    @Autowired
    private IIdentityService identityService;

    @Autowired
    private ImportHelper importHelper;

    // todo 2058
//    @BeforeEach
//    public void setUp() {
//        repository.deleteAll();
//    }
//
//    @AfterEach
//    public void cleanUp() {
//        repository.deleteAll();
//    }

    @Test
    public void removeExpired() {
        importHelper.createIdentity(IdentityParams.with()
                        .username(new TextField("test@test.com"))
                        .registrationToken(new TextField("token"))
                        .expirationDateTime(new DateTimeField(LocalDateTime.now().minusDays(10)))
                        .state(new EnumerationMapField(IdentityState.INVITED.name()))
                .build(), new ArrayList<>());

        importHelper.createIdentity(IdentityParams.with()
                        .username(new TextField("test2@test.com"))
                        .registrationToken(new TextField("token2"))
                        .state(new EnumerationMapField(IdentityState.INVITED.name()))
                .build(), new ArrayList<>());

        service.removeExpiredIdentities();

        assert identityService.findAll().size() == 1;
    }

    @Test
    public void authorizeToken() {
        importHelper.createIdentity(IdentityParams.with()
                .username(new TextField("test3@test.com"))
                .registrationToken(new TextField("token3"))
                .expirationDateTime(new DateTimeField(LocalDateTime.now().plusMinutes(10)))
                .state(new EnumerationMapField(IdentityState.INVITED.name()))
                .build(), new ArrayList<>());

        assert service.verifyToken(service.encodeToken("test3@test.com", "token3"));;
    }
}