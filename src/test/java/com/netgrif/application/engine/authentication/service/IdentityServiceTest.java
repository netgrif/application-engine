package com.netgrif.application.engine.authentication.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.IdentityState;
import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.domain.constants.SystemUserConstants;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authorization.domain.constants.UserConstants;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.DateTimeField;
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.SuperCreator;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class IdentityServiceTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IDataService dataService;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private SuperCreator superCreator;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
    }

    @Test
    void testFindById() throws InterruptedException {
        Identity identity = createIdentity("username");
        Thread.sleep(2000);
        assert identityService.findById(identity.getStringId()).isPresent();
        assert identityService.findById(new ObjectId().toString()).isEmpty();
    }

    @Test
    void testFindByUsername() throws InterruptedException {
        Identity identity = createIdentity("username");
        Thread.sleep(2000);
        assert identityService.findByUsername(identity.getUsername()).isPresent();
        assert identityService.findByUsername("wrongUsername").isEmpty();
    }

    @Test
    void testFindByLoggedIdentity() {
        assert identityService.findByLoggedIdentity(null).isEmpty();
        assert identityService.findByLoggedIdentity(LoggedIdentity.with()
                .username("username2")
                .password("password")
                .identityId(new ObjectId().toString())
                .build()).isEmpty();

        Identity identity = createIdentity("username");
        LoggedIdentity loggedIdentity = identity.toSession();

        Optional<Identity> foundIdentityOpt = identityService.findByLoggedIdentity(loggedIdentity);
        assert foundIdentityOpt.isPresent();
        assert foundIdentityOpt.get().getStringId().equals(identity.getStringId());
    }

    @Test
    void testExistsByUsername() throws InterruptedException {
        Identity identity = createIdentity("username");
        Thread.sleep(2000);
        assert identityService.existsByUsername(identity.getUsername());
        assert !identityService.existsByUsername("wrongUsername");
    }

    @Test
    void testFindActorIds() throws InterruptedException {
        Identity identity = createIdentity("username", List.of(new ObjectId().toString(), new ObjectId().toString()));
        Thread.sleep(2000);
        Set<String> actorIds = identityService.findActorIds(identity.getStringId());
        assert actorIds.size() == 3;
        assert actorIds.contains(identity.getMainActorId());
        assert actorIds.contains(identity.getAdditionalActorIds().get(0));
        assert actorIds.contains(identity.getAdditionalActorIds().get(1));
    }

    @Test
    void testFindAllByStateAndExpirationDateBefore() throws InterruptedException {
        Process identityProcess = petriNetService.getNewestVersionByIdentifier("identity");
        caseRepository.deleteAllByPetriNetObjectId(identityProcess.getId());

        doCreateIdentity(IdentityParams.with()
                .username(new TextField("username1"))
                .state(new EnumerationMapField(IdentityState.INVITED.name()))
                .expirationDateTime(new DateTimeField(LocalDateTime.now().minusDays(1)))
                .build());
        Identity identity = doCreateIdentity(IdentityParams.with()
                .username(new TextField("username2"))
                .state(new EnumerationMapField(IdentityState.BLOCKED.name()))
                .expirationDateTime(new DateTimeField(LocalDateTime.now().minusDays(1)))
                .build());
        doCreateIdentity(IdentityParams.with()
                .username(new TextField("username3"))
                .state(new EnumerationMapField(IdentityState.BLOCKED.name()))
                .build());
        doCreateIdentity(IdentityParams.with()
                .username(new TextField("username4"))
                .state(new EnumerationMapField(IdentityState.BLOCKED.name()))
                .expirationDateTime(new DateTimeField(LocalDateTime.now().plusDays(1)))
                .build());

        Thread.sleep(2000);
        List<Identity> identities = identityService.findAllByStateAndExpirationDateBefore(IdentityState.BLOCKED,
                LocalDateTime.now());
        assert identities.size() == 1;
        assert identities.get(0).getStringId().equals(identity.getStringId());

        assert identityService.findAllByStateAndExpirationDateBefore(null, null).isEmpty();
        assert identityService.findAllByStateAndExpirationDateBefore(IdentityState.BLOCKED, null).isEmpty();
        assert identityService.findAllByStateAndExpirationDateBefore(null, LocalDateTime.now()).isEmpty();
    }

    @Test
    void testFindAll() throws InterruptedException {
        Process identityProcess = petriNetService.getNewestVersionByIdentifier("identity");
        caseRepository.deleteAllByPetriNetObjectId(identityProcess.getId());

        assert identityService.findAll().isEmpty();

        int identityCount = 101; // probably more than a page size
        for (int i = 0; i < identityCount; ++i) {
            createIdentity(String.format("username%d", i));
        }

        Thread.sleep(2000);
        assert identityService.findAll().size() == identityCount;
    }


    @Test
    void testCreate() {
        assertThrows(IllegalArgumentException.class, () -> identityService.create(null));
        assertThrows(IllegalArgumentException.class, () -> identityService.create(IdentityParams.with()
                .username(new TextField(SystemUserConstants.EMAIL))
                .build()));
        assertThrows(IllegalArgumentException.class, () -> identityService.create(UserParams.with()
                .email(new TextField("wrong type of parameters"))
                .build()));
        assertThrows(IllegalArgumentException.class, () -> identityService.create(IdentityParams.with().build()));

        String username = "username";
        String firstname = "firstname";
        String lastname = "lastname";
        String password = "password";
        LocalDateTime expirationDateTime = LocalDateTime.now();
        String registrationToken = "token";
        String mainActorId = new ObjectId().toString();
        String additionalActorId = new ObjectId().toString();

        Identity identity = identityService.create(IdentityParams.with()
                .username(new TextField(username))
                .firstname(new TextField(firstname))
                .lastname(new TextField(lastname))
                .password(new TextField(password))
                .expirationDateTime(new DateTimeField(expirationDateTime))
                .registrationToken(new TextField(registrationToken))
                .mainActor(CaseField.withValue(List.of(mainActorId)))
                .additionalActors(CaseField.withValue(List.of(additionalActorId)))
                .build());

        assert identity != null;
        assert identity.getUsername().equals(username);
        assert identity.getFirstname().equals(firstname);
        assert identity.getLastname().equals(lastname);
        assert identity.getPassword().equals(password); // not encoded
        assert identity.getFullName().equals(String.join(" ", firstname, lastname));
        assert identity.getExpirationDate().toLocalDate().equals(expirationDateTime.toLocalDate());
        assert identity.getRegistrationToken().equals(registrationToken);
        assert identity.getMainActorId().equals(mainActorId);
        assert identity.getAdditionalActorIds().size() == 1;
        assert identity.getAdditionalActorIds().get(0).equals(additionalActorId);
        assert identity.isActive();
    }

    @Test
    void testCreateWithDefaultUser() {
        Identity identity = identityService.createWithDefaultUser(IdentityParams.with()
                .username(new TextField("username"))
                .password(new TextField("password"))
                .firstname(new TextField("firstname"))
                .lastname(new TextField("lastname"))
                .build());

        assert ObjectId.isValid(identity.getMainActorId());
        assert workflowService.findOne(identity.getMainActorId()).getProcessIdentifier().equals(UserConstants.PROCESS_IDENTIFIER);
    }

    @Test
    void testEncodePasswordAndCreate() {
        String password = "password";

        Identity identity = identityService.encodePasswordAndCreate(IdentityParams.with()
                .username(new TextField("username"))
                .password(new TextField(password))
                .build());

        assert identity.getPassword() != null;
        assert !identity.getPassword().equals(password);
    }

    @Test
    void testUpdate() {
        assertThrows(IllegalArgumentException.class, () -> identityService.update(null, IdentityParams.with()
                .username(new TextField("username"))
                .build()));

        String username = "username";
        final Identity identity = doCreateIdentity(IdentityParams.with()
                .username(new TextField("username"))
                .build());

        assert identity.getUsername().equals(username);
        assert identity.getFirstname() == null;
        assert identity.getLastname() == null;

        assertThrows(IllegalArgumentException.class, () -> identityService.update(identity, null));
        assertThrows(IllegalArgumentException.class, () -> identityService.update(identity, IdentityParams.with()
                .username(new TextField(null))
                .build()));

        String firstname = "firstname";
        String lastname = "lastname";
        Identity updatedIdentity = identityService.update(identity, IdentityParams.with()
                .username(new TextField("username"))
                .firstname(new TextField(firstname))
                .lastname(new TextField(lastname))
                .build());

        assert updatedIdentity.getStringId().equals(identity.getStringId());
        assert updatedIdentity.getUsername().equals(username);
        assert updatedIdentity.getFirstname().equals(firstname);
        assert updatedIdentity.getLastname().equals(lastname);
    }

    @Test
    void testEncodePasswordAndUpdate() {
        assertThrows(IllegalArgumentException.class, () -> identityService.encodePasswordAndUpdate(null, IdentityParams.with()
                .username(new TextField("username"))
                .password(new TextField("password"))
                .build()));

        final Identity identity = doCreateIdentity(IdentityParams.with()
                .username(new TextField("username"))
                .build());

        String password = "password";
        Identity updatedIdentity = identityService.encodePasswordAndUpdate(identity, IdentityParams.with()
                .username(new TextField("username"))
                .password(new TextField(password))
                .build());

        assert updatedIdentity.getPassword() != null;
        assert !updatedIdentity.getPassword().equals(password);
    }

    @Test
    void testAddAdditionalActor() {
        String actorId = new ObjectId().toString();
        assertThrows(IllegalArgumentException.class , () -> identityService.addAdditionalActor(null, actorId));

        final Identity identity = createIdentity("username");

        assertThrows(IllegalArgumentException.class , () -> identityService.addAdditionalActor(identity, null));

        Identity updatedIdentity = identityService.addAdditionalActor(identity, actorId);
        assert updatedIdentity.getAdditionalActorIds().size() == 1;
        assert updatedIdentity.getAdditionalActorIds().get(0).equals(actorId);

        String actorId2 = new ObjectId().toString();
        updatedIdentity = identityService.addAdditionalActor(updatedIdentity, actorId2);
        assert updatedIdentity.getAdditionalActorIds().size() == 2;
        assert updatedIdentity.getAdditionalActorIds().contains(actorId2);
    }

    @Test
    void testAddAdditionalActors() {
        String actorId1 = new ObjectId().toString();
        String actorId2 = new ObjectId().toString();
        assertThrows(IllegalArgumentException.class , () -> identityService.addAdditionalActors(null,
                Set.of(actorId1, actorId2)));

        final Identity identity = createIdentity("username");

        assertThrows(IllegalArgumentException.class , () -> identityService.addAdditionalActors(identity, null));
        assertThrows(IllegalArgumentException.class , () -> identityService.addAdditionalActors(identity, null));

        Identity updatedIdentity = identityService.addAdditionalActors(identity, Set.of(actorId1, actorId2));
        assert updatedIdentity.getAdditionalActorIds().size() == 2;
        assert updatedIdentity.getAdditionalActorIds().contains(actorId1);
        assert updatedIdentity.getAdditionalActorIds().contains(actorId2);

        String actorId3 = new ObjectId().toString();
        String actorId4 = new ObjectId().toString();
        updatedIdentity = identityService.addAdditionalActors(updatedIdentity, Set.of(actorId3, actorId4));
        assert updatedIdentity.getAdditionalActorIds().size() == 4;
        assert updatedIdentity.getAdditionalActorIds().contains(actorId3);
        assert updatedIdentity.getAdditionalActorIds().contains(actorId4);
    }

    @Test
    void testRemoveAllByStateAndExpirationDateBefore() throws InterruptedException {
        Process identityProcess = petriNetService.getNewestVersionByIdentifier("identity");
        caseRepository.deleteAllByPetriNetObjectId(identityProcess.getId());

        doCreateIdentity(IdentityParams.with()
                .username(new TextField("username1"))
                .state(new EnumerationMapField(IdentityState.INVITED.name()))
                .expirationDateTime(new DateTimeField(LocalDateTime.now().minusDays(1)))
                .build());
        Identity identity = doCreateIdentity(IdentityParams.with()
                .username(new TextField("username2"))
                .state(new EnumerationMapField(IdentityState.BLOCKED.name()))
                .expirationDateTime(new DateTimeField(LocalDateTime.now().minusDays(1)))
                .build());
        doCreateIdentity(IdentityParams.with()
                .username(new TextField("username3"))
                .state(new EnumerationMapField(IdentityState.BLOCKED.name()))
                .build());
        doCreateIdentity(IdentityParams.with()
                .username(new TextField("username4"))
                .state(new EnumerationMapField(IdentityState.BLOCKED.name()))
                .expirationDateTime(new DateTimeField(LocalDateTime.now().plusDays(1)))
                .build());

        Thread.sleep(2000);

        long countBefore = caseRepository.count();

        assertThrows(IllegalArgumentException.class, () -> identityService.removeAllByStateAndExpirationDateBefore(null, null));
        assertThrows(IllegalArgumentException.class, () -> identityService.removeAllByStateAndExpirationDateBefore(IdentityState.BLOCKED, null));
        assertThrows(IllegalArgumentException.class, () -> identityService.removeAllByStateAndExpirationDateBefore(null, LocalDateTime.now()));

        assert caseRepository.count() == countBefore;

        TestHelper.login(superCreator.getSuperIdentity());
        List<Identity> removedIdentities = identityService.removeAllByStateAndExpirationDateBefore(IdentityState.BLOCKED,
                LocalDateTime.now());

        assert removedIdentities.size() == 1;
        assert removedIdentities.get(0).getStringId().equals(identity.getStringId());
        assertThrows(IllegalArgumentException.class, () -> workflowService.findOne(identity.getStringId()));
    }

    @Test
    void testForbiddenKeywords() {
        assert !identityService.registerForbiddenKeywords(null);
        assert !identityService.registerForbiddenKeywords(Set.of());

        assert !identityService.removeForbiddenKeywords(null);
        assert !identityService.removeForbiddenKeywords(Set.of());

        Set<String> keywords = Set.of("keyword1", "keyword2", "keyword3");
        assert !identityService.removeForbiddenKeywords(keywords);
        assert identityService.registerForbiddenKeywords(keywords);

        assertThrows(IllegalArgumentException.class, () -> identityService.create(IdentityParams.with()
                .username(new TextField("keyword1"))
                .build()));

        assertThrows(IllegalArgumentException.class, () -> identityService.create(IdentityParams.with()
                .username(new TextField("keyword2"))
                .build()));

        assertThrows(IllegalArgumentException.class, () -> identityService.create(IdentityParams.with()
                .username(new TextField("keyword3"))
                .build()));

        assert identityService.removeForbiddenKeywords(Set.of("keyword1", "keyword2"));

        Identity identity = identityService.create(IdentityParams.with().username(new TextField("keyword1")).build());
        assert identity != null;

        identity = identityService.create(IdentityParams.with().username(new TextField("keyword2")).build());
        assert identity != null;

        assertThrows(IllegalArgumentException.class, () -> identityService.create(IdentityParams.with()
                .username(new TextField("keyword3"))
                .build()));

        identityService.clearForbiddenKeywords();

        identity = identityService.create(IdentityParams.with().username(new TextField("keyword3")).build());
        assert identity != null;
    }

    @Test
    void testRemoveForbiddenKeywords() {
        assert !identityService.removeForbiddenKeywords(null);
        assert !identityService.removeForbiddenKeywords(Set.of());

        Set<String> keywords = Set.of("keyword1", "keyword2");
        assert identityService.registerForbiddenKeywords(keywords);

        assertThrows(IllegalArgumentException.class, () -> identityService.create(IdentityParams.with()
                .username(new TextField("keyword1"))
                .build()));

        assertThrows(IllegalArgumentException.class, () -> identityService.create(IdentityParams.with()
                .username(new TextField("keyword2"))
                .build()));
    }

    private Identity createIdentity(String username) {
        return doCreateIdentity(IdentityParams.with()
                .username(new TextField(username))
                .password(new TextField("password"))
                .firstname(new TextField("firstname"))
                .lastname(new TextField("lastname"))
                .mainActor(CaseField.withValue(List.of(new ObjectId().toString())))
                .build());
    }

    private Identity createIdentity(String username, List<String> additionalActorIds) {
        return doCreateIdentity(IdentityParams.with()
                .username(new TextField(username))
                .password(new TextField("password"))
                .firstname(new TextField("firstname"))
                .lastname(new TextField("lastname"))
                .mainActor(CaseField.withValue(List.of(new ObjectId().toString())))
                .additionalActors(CaseField.withValue(additionalActorIds))
                .build());
    }

    private Identity doCreateIdentity(IdentityParams params) {
        Case identityCase = workflowService.createCaseByIdentifier("identity", params.getUsername().getRawValue(),
                "", null).getCase();
        return new Identity(dataService.setData(identityCase, params.toDataSet(), null).getCase());
    }

}
