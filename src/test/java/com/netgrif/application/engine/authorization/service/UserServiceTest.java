package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.domain.constants.SystemUserConstants;
import com.netgrif.application.engine.authorization.domain.Group;
import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.constants.GroupConstants;
import com.netgrif.application.engine.authorization.domain.constants.UserConstants;
import com.netgrif.application.engine.authorization.domain.params.GroupParams;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.DefaultGroupRunner;
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

import java.util.*;

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

    @Autowired
    private DefaultGroupRunner defaultGroupRunner;

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
    void getSystemUser() {
        User systemUser = userService.getSystemUser();

        assert systemUser != null;
        assert systemUser.getEmail().equals(SystemUserConstants.EMAIL);
        assert systemUser.getFirstname().equals(SystemUserConstants.FIRSTNAME);
        assert systemUser.getLastname().equals(SystemUserConstants.LASTNAME);
        assert systemUser.getEmail().equals(SystemUserConstants.EMAIL);
        assert systemUser.getFullName().equals(String.join(" ", SystemUserConstants.FIRSTNAME,
                SystemUserConstants.LASTNAME));
        assert ObjectId.isValid(systemUser.getStringId());
    }

    @Test
    void testCreate() {
        String email = "some@email.com";
        String firstname = "firstname";
        String lastname = "lastname";
        String propertyKey = "property";
        String propertyValue = "isActive";
        User user = userService.create(UserParams.with()
                .email(new TextField(email))
                .firstname(new TextField(firstname))
                .lastname(new TextField(lastname))
                .properties(Map.of(propertyKey, propertyValue))
                .build());

        assert user != null && user.getCase() != null;
        assert user.getEmail().equals(email);
        assert user.getFirstname().equals(firstname);
        assert user.getLastname().equals(lastname);
        assert user.getGroupIds() != null;
        assert user.getGroupIds().size() == 1;
        assert user.getGroupIds().get(0).equals(defaultGroupRunner.getDefaultGroup().getStringId());
        assert user.getCase().getProperties() != null;
        assert user.getCase().getProperties().size() == 1;
        assert user.getCase().getProperties().containsKey(propertyKey);
        assert user.getCase().getProperties().get(propertyKey).equals(propertyValue);

        assertThrows(IllegalArgumentException.class, () -> userService.create(UserParams.with()
                .firstname(new TextField(firstname))
                .lastname(new TextField(lastname))
                .build()));

        assertThrows(IllegalArgumentException.class, () -> userService.create(UserParams.with()
                .email(new TextField(SystemUserConstants.EMAIL))
                .build()));

        assertThrows(IllegalArgumentException.class, () -> userService.create(GroupParams.with()
                .name(new TextField("wrong type of parameters"))
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
        assert user.getGroupIds() != null;
        assert user.getGroupIds().size() == 1;
        assert user.getGroupIds().get(0).equals(defaultGroupRunner.getDefaultGroup().getStringId());
        assert user.getCase().getProperties() == null || user.getCase().getProperties().isEmpty();

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

        String newFirstname = "newFirstname";
        String newLastname = "newLastname";
        String propertyKey = "property";
        String propertyValue = "isActive";
        Group testGroup = createGroup("test group");
        User updatedUser = userService.update(user, UserParams.with()
                .email(new TextField(email))
                .firstname(new TextField(newFirstname))
                .lastname(new TextField(newLastname))
                .groupIds(CaseField.withValue(List.of(testGroup.getStringId())))
                .properties(Map.of(propertyKey, propertyValue))
                .build());

        assert user.getStringId().equals(updatedUser.getStringId());
        assert updatedUser.getEmail().equals(email);
        assert updatedUser.getFirstname().equals(newFirstname);
        assert updatedUser.getLastname().equals(newLastname);
        assert updatedUser.getGroupIds() != null;
        assert updatedUser.getGroupIds().size() == 1;
        assert updatedUser.getGroupIds().get(0).equals(testGroup.getStringId());
        assert updatedUser.getCase().getProperties() != null;
        assert updatedUser.getCase().getProperties().size() == 1;
        assert updatedUser.getCase().getProperties().containsKey(propertyKey);
        assert updatedUser.getCase().getProperties().get(propertyKey).equals(propertyValue);
    }

    @Test
    void testAddGroup() {
        Group group = createGroup("test group");
        User user = createUser("test@user.com");
        assert user.getGroupIds() != null;
        assert user.getGroupIds().size() == 1;

        assertThrows(IllegalArgumentException.class, () -> userService.addGroup(null, group.getStringId()));
        final User finalUser = user;
        assertThrows(IllegalArgumentException.class, () -> userService.addGroup(finalUser, null));

        user = userService.addGroup(user, group.getStringId());
        assert user.getGroupIds() != null;
        assert user.getGroupIds().size() == 2;
        assert user.getGroupIds().contains(defaultGroupRunner.getDefaultGroup().getStringId());
        assert user.getGroupIds().contains(group.getStringId());
    }

    @Test
    void testAddGroups() {
        Group group1 = createGroup("test group 1");
        Group group2 = createGroup("test group 2");
        User user = createUser("test@user.com");
        assert user.getGroupIds() != null;
        assert user.getGroupIds().size() == 1;

        assertThrows(IllegalArgumentException.class, () -> userService.addGroups(null, Set.of(group1.getStringId(),
                group2.getStringId())));
        final User finalUser = user;
        assertThrows(IllegalArgumentException.class, () -> userService.addGroups(finalUser, null));

        Set<String> groupIdsToAdd = new HashSet<>();
        groupIdsToAdd.add(group1.getStringId());
        groupIdsToAdd.add(group2.getStringId());
        groupIdsToAdd.add(defaultGroupRunner.getDefaultGroup().getStringId());
        groupIdsToAdd.add(null);

        user = userService.addGroups(user, groupIdsToAdd);
        assert user.getGroupIds() != null;
        assert user.getGroupIds().size() == 3;
        assert user.getGroupIds().contains(defaultGroupRunner.getDefaultGroup().getStringId());
        assert user.getGroupIds().contains(group1.getStringId());
        assert user.getGroupIds().contains(group2.getStringId());
    }

    @Test
    void testRemoveGroup() {
        Group group = createGroup("test group");
        User user = createUser("test@user.com", List.of(group.getStringId()));
        assert user.getGroupIds() != null;
        assert user.getGroupIds().size() == 2;

        assertThrows(IllegalArgumentException.class, () -> userService.removeGroup(null, group.getStringId()));
        final User finalUser = user;
        assertThrows(IllegalArgumentException.class, () -> userService.removeGroup(finalUser, null));

        user = userService.removeGroup(user, group.getStringId());
        assert user.getGroupIds() != null;
        assert user.getGroupIds().size() == 1;
        assert user.getGroupIds().get(0).equals(defaultGroupRunner.getDefaultGroup().getStringId());

        user = userService.removeGroup(user, defaultGroupRunner.getDefaultGroup().getStringId());
        assert user.getGroupIds() == null || user.getGroupIds().isEmpty();
    }

    @Test
    void testRemoveGroups() {
        Group group1 = createGroup("test group 1");
        Group group2 = createGroup("test group 2");
        User user = createUser("test@user.com", List.of(group1.getStringId(), group2.getStringId()));
        assert user.getGroupIds() != null;
        assert user.getGroupIds().size() == 3;

        assertThrows(IllegalArgumentException.class, () -> userService.removeGroups(null, Set.of(group1.getStringId())));
        final User finalUser = user;
        assertThrows(IllegalArgumentException.class, () -> userService.removeGroups(finalUser, null));

        Set<String> groupIdsToRemove = new HashSet<>(Set.of(group1.getStringId(), group2.getStringId(),
                defaultGroupRunner.getDefaultGroup().getStringId()));
        groupIdsToRemove.add(null);

        user = userService.removeGroups(user, groupIdsToRemove);
        assert user.getGroupIds() == null || user.getGroupIds().isEmpty();
    }

    @Test
    void testForbiddenKeywords() {
        assert !userService.registerForbiddenKeywords(null);
        assert !userService.registerForbiddenKeywords(Set.of());

        assert !userService.removeForbiddenKeywords(null);
        assert !userService.removeForbiddenKeywords(Set.of());

        Set<String> keywords = Set.of("keyword1", "keyword2", "keyword3");
        assert !userService.removeForbiddenKeywords(keywords);
        assert userService.registerForbiddenKeywords(keywords);

        assertThrows(IllegalArgumentException.class, () -> userService.create(UserParams.with()
                .email(new TextField("keyword1"))
                .build()));

        assertThrows(IllegalArgumentException.class, () -> userService.create(UserParams.with()
                .email(new TextField("keyword2"))
                .build()));

        assertThrows(IllegalArgumentException.class, () -> userService.create(UserParams.with()
                .email(new TextField("keyword3"))
                .build()));

        assert userService.removeForbiddenKeywords(Set.of("keyword1", "keyword2"));

        User user = userService.create(UserParams.with().email(new TextField("keyword1")).build());
        assert user != null;

        user = userService.create(UserParams.with().email(new TextField("keyword2")).build());
        assert user != null;

        assertThrows(IllegalArgumentException.class, () -> userService.create(UserParams.with()
                .email(new TextField("keyword3"))
                .build()));

        userService.clearForbiddenKeywords();

        user = userService.create(UserParams.with().email(new TextField("keyword3")).build());
        assert user != null;
    }

    private User createUser(String email) {
        return createUser(email, new ArrayList<>());
    }

    private User createUser(String email, List<String> additionalGroupIds) {
        Case userCase = workflowService.createCaseByIdentifier(UserConstants.PROCESS_IDENTIFIER, email, "", null).getCase();
        List<String> groupIds = new ArrayList<>(additionalGroupIds);
        groupIds.add(defaultGroupRunner.getDefaultGroup().getStringId());
        return new User(dataService.setData(userCase, UserParams.with()
                .email(new TextField(email))
                .groupIds(CaseField.withValue(groupIds))
                .build()
                .toDataSet(), null).getCase());
    }

    private Group createGroup(String name) {
        Case groupCase = workflowService.createCaseByIdentifier(GroupConstants.PROCESS_IDENTIFIER, name, "", null).getCase();
        return new Group(dataService.setData(groupCase, GroupParams.with()
                .name(new TextField(name))
                .build()
                .toDataSet(), null).getCase());
    }
}
