package com.netgrif.application.engine.search;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.IUser;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private IUser createUser(String name, String surname, String email, String authority) {
        User user = new User(email, "password", name, surname);
        Authority[] authorities = new Authority[]{auths.get(authority)};
        ProcessRole[] processRoles = new ProcessRole[]{};
        return importHelper.createUser(user, authorities, processRoles);
    }

    private static IUser convertToIUser(Object userObject) {
        assert userObject instanceof IUser;
        return (IUser) userObject;
    }

    private static List<IUser> convertToIUserList(Object userListObject) {
        assert userListObject instanceof List<?>;
        for (Object userObject : (List<?>) userListObject) {
            assert userObject instanceof IUser;
        }

        return (List<IUser>) userListObject;
    }

    private void compareUsers(IUser actual, IUser expected) {
        assert actual.getStringId().equals(expected.getStringId());
    }

    private void compareUsers(IUser actual, List<IUser> expected) {
        List<String> expectedStringIds = expected.stream().map(IUser::getStringId).collect(Collectors.toList());

        assert expectedStringIds.contains(actual.getStringId());
    }

    private void compareUsers(List<IUser> actual, List<IUser> expected) {
        List<String> actualStringIds = actual.stream().map(IUser::getStringId).collect(Collectors.toList());
        List<String> expectedStringIds = expected.stream().map(IUser::getStringId).collect(Collectors.toList());

        assert actualStringIds.containsAll(expectedStringIds);
    }

    @Test
    public void testSearchById() {
        IUser user1 = createUser("name1", "surname1", "email1", "user");
        IUser user2 = createUser("name2", "surname2", "email2", "admin");

        String query = String.format("user: id eq '%s'", user1.getStringId());

        long count = searchService.count(query);
        assert count == 1;

        Object foundUser = searchService.search(query);
        compareUsers(convertToIUser(foundUser), user1);
    }

    @Test
    public void testSearchByEmail() {
        IUser user1 = createUser("name1", "surname1", "email1", "user");
        IUser user2 = createUser("name2", "surname2", "email2", "admin");

        String query = String.format("user: email eq '%s'", user1.getEmail());

        long count = searchService.count(query);
        assert count == 1;

        Object foundUser = searchService.search(query);
        compareUsers(convertToIUser(foundUser), user1);
    }

    @Test
    public void testSearchByName() {
        IUser user1 = createUser("name1", "surname1", "email1", "user");
        IUser user2 = createUser("name2", "surname2", "email2", "admin");
        IUser user3 = createUser("name1", "surname1", "email3", "user");

        String query = String.format("users: name eq '%s'", user1.getName());

        long count = searchService.count(query);
        assert count == 2;

        Object foundUsers = searchService.search(query);
        compareUsers(convertToIUserList(foundUsers), List.of(user1, user3));
    }

    @Test
    public void testSearchBySurname() {
        IUser user1 = createUser("name1", "surname1", "email1", "user");
        IUser user2 = createUser("name2", "surname2", "email2", "admin");
        IUser user3 = createUser("name1", "surname1", "email3", "user");

        String query = String.format("users: surname eq '%s'", user1.getSurname());

        long count = searchService.count(query);
        assert count == 2;

        Object foundUsers = searchService.search(query);
        compareUsers(convertToIUserList(foundUsers), List.of(user1, user3));
    }

}
