package com.netgrif.application.engine.authentication.service;

import com.netgrif.application.engine.TestHelper;
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
import com.netgrif.application.engine.startup.SuperCreator;
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
import java.util.ArrayList;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test"})
@SpringBootTest
public class TokenServiceTest {

    // todo: release/8.0.0 should be implemented in RegistrationServiceTest?

    @Autowired
    private IRegistrationService service;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IIdentityService identityService;

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private SuperCreator superCreator;

    @BeforeEach
    public void setUp() {
        testHelper.truncateDbs();
    }

    @Test
    public void removeExpired() throws InterruptedException {
        TestHelper.login(superCreator.getSuperIdentity());

        Identity identity1 = importHelper.createIdentity(IdentityParams.with()
                        .username(new TextField("test@test.com"))
                        .registrationToken(new TextField("token"))
                        .expirationDateTime(new DateTimeField(LocalDateTime.now().minusDays(10)))
                        .state(new EnumerationMapField(IdentityState.INVITED.name()))
                .build(), new ArrayList<>());

        Identity identity2 = importHelper.createIdentity(IdentityParams.with()
                        .username(new TextField("test2@test.com"))
                        .registrationToken(new TextField("token2"))
                        .state(new EnumerationMapField(IdentityState.INVITED.name()))
                .build(), new ArrayList<>());
        Thread.sleep(2000);

        service.removeExpiredIdentities();

        assert identityService.findById(identity1.getStringId()).isEmpty();
        assert identityService.findById(identity2.getStringId()).isPresent();
    }

    @Test
    public void authorizeToken() throws InterruptedException {
        Identity identity = importHelper.createIdentity(IdentityParams.with()
                .username(new TextField("test3@test.com"))
                .registrationToken(new TextField("token3"))
                .expirationDateTime(new DateTimeField(LocalDateTime.now().plusMinutes(10)))
                .state(new EnumerationMapField(IdentityState.INVITED.name()))
                .build(), new ArrayList<>());
        Thread.sleep(2000);

        assert service.verifyToken(service.encodeToken(identity.getUsername(), identity.getRegistrationToken()));;
    }
}