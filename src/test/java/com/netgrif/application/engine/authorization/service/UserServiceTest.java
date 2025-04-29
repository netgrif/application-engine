package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.constants.UserConstants;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
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
public class UserServiceTest {

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
    private UserService userService;

    @BeforeEach
    void before() {
        testHelper.truncateDbs();
    }

    @Test
    void testFindByEmail() throws InterruptedException {
        assert userService.findByEmail(null).isEmpty();
        assert userService.findByEmail("nonexisting@email.com").isEmpty();

        String email = "some@email.com";
        createUser(email);

        Thread.sleep(2000);
        assert userService.findByEmail(email).isPresent();
    }

    @Test
    void testExistsByEmail() throws InterruptedException {
        assert !userService.existsByEmail(null);
        assert !userService.existsByEmail("nonexisting@email.com");

        String email = "some@email.com";
        createUser(email);

        Thread.sleep(2000);
        assert userService.existsByEmail(email);
    }

    @Test
    void testFindById() throws InterruptedException {
        assert userService.findById(null).isEmpty();
        assert userService.findById(new ObjectId().toString()).isEmpty();

        String email = "some@email.com";
        User user = createUser(email);

        Thread.sleep(2000);
        assert userService.findById(user.getStringId()).isPresent();
    }

    @Test
    void testExistsById() {
        assert !userService.existsById(null);
        assert !userService.existsById(new ObjectId().toString());

        String email = "some@email.com";
        User user = createUser(email);

        assert userService.existsById(user.getStringId());
    }

    @Test
    void testFindAll() {
        Process userProcess = petriNetService.getNewestVersionByIdentifier(UserConstants.PROCESS_IDENTIFIER);
        caseRepository.deleteAllByPetriNetObjectId(userProcess.getId());

        assert userService.findAll().isEmpty();

        createUser("some@email.com");
        createUser("some@email2.com");

        assert userService.findAll().size() == 2;
    }

    @Test
    void testCreate() {
        String email = "some@email.com";
        String firstname = "firstname";
        String lastname = "lastname";
        User user = userService.create(UserParams.with()
                .email(new TextField(email))
                .firstname(new TextField(firstname))
                .lastname(new TextField(lastname))
                .build());

        assert user != null && user.getCase() != null;
        assert user.getEmail().equals(email);
        assert user.getFirstname().equals(firstname);
        assert user.getLastname().equals(lastname);

        assertThrows(IllegalArgumentException.class, () -> userService.create(UserParams.with()
                .firstname(new TextField(firstname))
                .lastname(new TextField(lastname))
                .build()));

        assertThrows(IllegalArgumentException.class, () -> userService.create(null));
    }

    @Test
    void testUpdate() {
        String email = "some@email.com";
        User user = createUser(email);
        assert user.getEmail().equals(email);
        assert user.getFirstname() == null;
        assert user.getLastname() == null;

        String newFirstname = "newFirstname";
        String newLastname = "newLastname";
        User updatedUser = userService.update(user, UserParams.with()
                .email(new TextField(email))
                .firstname(new TextField(newFirstname))
                .lastname(new TextField(newLastname))
                .build());

        assert user.getStringId().equals(updatedUser.getStringId());
        assert updatedUser.getEmail().equals(email);
        assert updatedUser.getFirstname().equals(newFirstname);
        assert updatedUser.getLastname().equals(newLastname);

        assertThrows(IllegalArgumentException.class, () -> userService.update(user, UserParams.with()
                .email(new TextField(null))
                .firstname(new TextField("firstname"))
                .lastname(new TextField("lastname"))
                .build()));

        assertThrows(IllegalArgumentException.class, () -> userService.update(user, null));
        assertThrows(IllegalArgumentException.class, () -> userService.update(null, UserParams.with()
                .email(new TextField("email"))
                .firstname(new TextField("firstname"))
                .lastname(new TextField("lastname"))
                .build()));
    }

    private User createUser(String email) {
        Case userCase = workflowService.createCaseByIdentifier(UserConstants.PROCESS_IDENTIFIER, email, "", null).getCase();
        return new User(dataService.setData(userCase, UserParams.with()
                .email(new TextField(email))
                .build()
                .toDataSet(), null).getCase());
    }
}
