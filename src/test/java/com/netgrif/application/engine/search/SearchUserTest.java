package com.netgrif.application.engine.search;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.search.interfaces.ISearchService;
import com.netgrif.application.engine.startup.ImportHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.netgrif.application.engine.search.utils.SearchTestUtils.*;

@Slf4j
@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class SearchUserTest {

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ISearchService searchService;

    private Map<String, Authority> auths;

    @BeforeEach
    void setup() {
        testHelper.truncateDbs();
        auths = importHelper.createAuthorities(Map.of("user", Authority.user, "admin", Authority.admin));
    }

    private User createUser(String name, String surname, String email, String authority) {
        User user = new User(email, "password", name, surname);
        Authority[] authorities = new Authority[]{auths.get(authority)};
        ProcessRole[] processRoles = new ProcessRole[]{};
        return (User) importHelper.createUser(user, authorities, processRoles);
    }

    private void searchAndCompare(String query, User expected) {
        long count = searchService.count(query);
        assert count == 1;

        Object actual = searchService.search(query);
        compareById(convertToObject(actual, User.class), expected, User::getStringId);
    }

    private void searchAndCompare(String query, List<User> expected) {
        long count = searchService.count(query);
        assert count == expected.size();

        Object actual = searchService.search(query);
        compareById(convertToObject(actual, User.class), expected, User::getStringId);
    }

    private void searchAndCompareAsList(String query, List<User> expected) {
        long count = searchService.count(query);
        assert count == expected.size();

        Object actual = searchService.search(query);
        compareById(convertToObjectList(actual, User.class), expected, User::getStringId);
    }

    private void searchAndCompareAsListInOrder(String query, List<User> expected) {
        long count = searchService.count(query);
        assert count == expected.size();

        Object actual = searchService.search(query);
        compareByIdInOrder(convertToObjectList(actual, User.class), expected, User::getStringId);
    }

    @Test
    public void testSearchById() {
        User user1 = createUser("name1", "surname1", "email1", "user");
        User user2 = createUser("name2", "surname2", "email2", "admin");

        String query = String.format("user: id eq '%s'", user1.getStringId());

        searchAndCompare(query, user1);

        // in list
        String queryInList = String.format("users: id in ('%s', '%s')", user1.getStringId(), user2.getStringId());

        searchAndCompareAsList(queryInList, List.of(user1, user2));

        // sort
        String querySort = String.format("users: id in ('%s', '%s') sort by id", user1.getStringId(), user2.getStringId());
        String querySort2 = String.format("users: id in ('%s', '%s') sort by id desc", user1.getStringId(), user2.getStringId());

        searchAndCompareAsListInOrder(querySort, List.of(user1, user2));
        searchAndCompareAsListInOrder(querySort2, List.of(user2, user1));
    }

    @Test
    public void testSearchByEmail() {
        User user1 = createUser("name1", "surname1", "email1", "user");
        User user2 = createUser("name2", "surname2", "email2", "admin");

        String query = String.format("user: email eq '%s'", user1.getEmail());

        searchAndCompare(query, user1);

        // in list
        String queryInList = String.format("users: email in ('%s', '%s')", user1.getEmail(), user2.getEmail());

        searchAndCompareAsList(queryInList, List.of(user1, user2));

        // sort
        String querySort = String.format("users: id in ('%s', '%s') sort by email", user1.getStringId(), user2.getStringId());
        String querySort2 = String.format("users: id in ('%s', '%s') sort by email desc", user1.getStringId(), user2.getStringId());

        searchAndCompareAsListInOrder(querySort, List.of(user1, user2));
        searchAndCompareAsListInOrder(querySort2, List.of(user2, user1));
    }

    @Test
    public void testSearchByName() {
        User user1 = createUser("name1", "surname1", "email1", "user");
        User user2 = createUser("name2", "surname2", "email2", "admin");
        User user3 = createUser("name1", "surname1", "email3", "user");

        String query = String.format("users: name eq '%s'", user1.getName());

        searchAndCompareAsList(query, List.of(user1, user3));

        // in list
        String queryInList = String.format("users: name in ('%s', '%s')", user1.getName(), user2.getName());

        searchAndCompareAsList(queryInList, List.of(user1, user2, user3));

        // in range
        String queryInRange = String.format("users: name in ('%s' : '%s']", user1.getName(), user2.getName());

        searchAndCompareAsList(queryInRange, List.of(user2));

        // sort
        String querySort = String.format("users: id in ('%s', '%s', '%s') sort by name", user1.getStringId(), user2.getStringId(), user3.getStringId());
        String querySort2 = String.format("users: id in ('%s', '%s', '%s') sort by name desc", user1.getStringId(), user2.getStringId(), user3.getStringId());

        searchAndCompareAsListInOrder(querySort, List.of(user1, user3, user2));
        searchAndCompareAsListInOrder(querySort2, List.of(user2, user1, user3));
    }

    @Test
    public void testSearchBySurname() {
        User user1 = createUser("name1", "surname1", "email1", "user");
        User user2 = createUser("name2", "surname2", "email2", "admin");
        User user3 = createUser("name1", "surname1", "email3", "user");

        String query = String.format("users: surname eq '%s'", user1.getSurname());

        searchAndCompareAsList(query, List.of(user1, user3));

        // in list
        String queryInList = String.format("users: surname in ('%s', '%s')", user1.getSurname(), user2.getSurname());

        searchAndCompareAsList(queryInList, List.of(user1, user2, user3));

        // in range
        String queryInRange = String.format("users: surname in ('%s' : '%s']", user1.getSurname(), user2.getSurname());

        searchAndCompareAsList(queryInRange, List.of(user2));

        // sort
        String querySort = String.format("users: id in ('%s', '%s', '%s') sort by surname", user1.getStringId(), user2.getStringId(), user3.getStringId());
        String querySort2 = String.format("users: id in ('%s', '%s', '%s') sort by surname desc", user1.getStringId(), user2.getStringId(), user3.getStringId());

        searchAndCompareAsListInOrder(querySort, List.of(user1, user3, user2));
        searchAndCompareAsListInOrder(querySort2, List.of(user2, user1, user3));
    }

    @Test
    public void testPagination() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            users.add(createUser("name" + i, "surname" + i , "email" + i, "user"));
        }

        String queryOne = String.format("user: email contains '%s'", "email");
        String queryMore = String.format("users: email contains '%s'", "email");
        String queryMoreCustomPagination = String.format("users: email contains '%s' page 1 size 5", "email");

        long count = searchService.count(queryOne);
        assert count == 50;

        Object actual = searchService.search(queryOne);
        compareById(convertToObject(actual, User.class), users.get(0), User::getStringId);

        actual = searchService.search(queryMore);
        compareById(convertToObjectList(actual, User.class), users.subList(0, 19), User::getStringId);

        actual = searchService.search(queryMoreCustomPagination);
        compareById(convertToObjectList(actual, User.class), users.subList(5, 9), User::getStringId);
    }
}
