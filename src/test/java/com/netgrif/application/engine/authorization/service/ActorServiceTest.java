package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.params.ActorParams;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
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
import com.netgrif.application.engine.petrinet.domain.Process;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class ActorServiceTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private IDataService dataService;

    @Autowired
    private ActorService actorService;

    @BeforeEach
    void before() {
        testHelper.truncateDbs();
    }

    @Test
    void testFindByEmail() throws InterruptedException {
        assert actorService.findByEmail(null).isEmpty();
        assert actorService.findByEmail("nonexisting@email.com").isEmpty();

        String email = "some@email.com";
        createActor(email);

        Thread.sleep(2000);
        assert actorService.findByEmail(email).isPresent();
    }

    @Test
    void testExistsByEmail() throws InterruptedException {
        assert !actorService.existsByEmail(null);
        assert !actorService.existsByEmail("nonexisting@email.com");

        String email = "some@email.com";
        createActor(email);

        Thread.sleep(2000);
        assert actorService.existsByEmail(email);
    }

    @Test
    void testFindById() throws InterruptedException {
        assert actorService.findById(null).isEmpty();
        assert actorService.findById(new ObjectId().toString()).isEmpty();

        String email = "some@email.com";
        User user = createActor(email);

        Thread.sleep(2000);
        assert actorService.findById(user.getStringId()).isPresent();
    }

    @Test
    void testExistsById() {
        assert !actorService.existsById(null);
        assert !actorService.existsById(new ObjectId().toString());

        String email = "some@email.com";
        User user = createActor(email);

        assert actorService.existsById(user.getStringId());
    }

    @Test
    void testFindAll() {
        Process actorProcess = petriNetService.getNewestVersionByIdentifier("actor");
        caseRepository.deleteAllByPetriNetObjectId(actorProcess.getId());

        assert actorService.findAll().isEmpty();

        createActor("some@email.com");
        createActor("some@email2.com");

        assert actorService.findAll().size() == 2;
    }

    @Test
    void testCreate() {
        String email = "some@email.com";
        String firstname = "firstname";
        String lastname = "lastname";
        User user = actorService.create(ActorParams.with()
                .email(new TextField(email))
                .firstname(new TextField(firstname))
                .lastname(new TextField(lastname))
                .build());

        assert user != null && user.getCase() != null;
        assert user.getEmail().equals(email);
        assert user.getFirstname().equals(firstname);
        assert user.getLastname().equals(lastname);

        assertThrows(IllegalArgumentException.class, () -> actorService.create(ActorParams.with()
                .firstname(new TextField(firstname))
                .lastname(new TextField(lastname))
                .build()));

        assertThrows(IllegalArgumentException.class, () -> actorService.create(null));
    }

    @Test
    void testUpdate() {
        String email = "some@email.com";
        User user = createActor(email);
        assert user.getEmail().equals(email);
        assert user.getFirstname() == null;
        assert user.getLastname() == null;

        String newFirstname = "newFirstname";
        String newLastname = "newLastname";
        User updatedUser = actorService.update(user, ActorParams.with()
                .email(new TextField(email))
                .firstname(new TextField(newFirstname))
                .lastname(new TextField(newLastname))
                .build());

        assert user.getStringId().equals(updatedUser.getStringId());
        assert updatedUser.getEmail().equals(email);
        assert updatedUser.getFirstname().equals(newFirstname);
        assert updatedUser.getLastname().equals(newLastname);

        assertThrows(IllegalArgumentException.class, () -> actorService.update(user, ActorParams.with()
                .email(new TextField(null))
                .firstname(new TextField("firstname"))
                .lastname(new TextField("lastname"))
                .build()));

        assertThrows(IllegalArgumentException.class, () -> actorService.update(user, null));
        assertThrows(IllegalArgumentException.class, () -> actorService.update(null, ActorParams.with()
                .email(new TextField("email"))
                .firstname(new TextField("firstname"))
                .lastname(new TextField("lastname"))
                .build()));
    }

    private User createActor(String email) {
        Case actorCase = workflowService.createCaseByIdentifier("actor", email, "", null).getCase();
        return new User(dataService.setData(actorCase, ActorParams.with()
                .email(new TextField(email))
                .build()
                .toDataSet(), null).getCase());
    }
}
