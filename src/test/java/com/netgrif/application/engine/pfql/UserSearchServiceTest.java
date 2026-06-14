package com.netgrif.application.engine.pfql;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.pfql.domain.enums.QueryType;
import com.netgrif.application.engine.pfql.service.userresource.UserSearchService;
import com.netgrif.application.engine.startup.SuperCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class UserSearchServiceTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private UserSearchService userSearchService;

    @Autowired
    private SuperCreator superCreator;

    @BeforeEach
    protected void beforeEach() {
        testHelper.truncateDbs();
    }

    @Test
    public void queryResourceTypeTest() {
        assertEquals(QueryType.USER, userSearchService.getQueryResourceType());
    }

    @Test
    public void searchOneTest() {
        assertThrows(IllegalArgumentException.class, () -> userSearchService.searchOne((String) null));
        assertThrows(IllegalArgumentException.class, () -> userSearchService.searchOne("users: title eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> userSearchService.searchOne("process: identifier eq 'query_lang_test'"));

        IUser result = userSearchService.searchOne("user: email eq '" + superCreator.getSuperUser().getEmail() + "'");
        assertNotNull(result);
        assertEquals(superCreator.getSuperUser().getStringId(), result.getStringId());

        result = userSearchService.searchOne("user: email eq 'wrong'");
        assertNull(result);
    }

    @Test
    public void searchAllTest() {
        assertThrows(IllegalArgumentException.class, () -> userSearchService.searchAll((String) null));
        assertThrows(IllegalArgumentException.class, () -> userSearchService.searchAll("user: title eq 'test'"));
        assertThrows(IllegalArgumentException.class, () -> userSearchService.searchAll("processes: identifier eq 'query_lang_test'"));

        Page<IUser> result = userSearchService.searchAll("users: email eq '" + superCreator.getSuperUser().getEmail() + "'");
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(superCreator.getSuperUser().getStringId(), result.getContent().get(0).getStringId());

        result = userSearchService.searchAll("users: email eq 'wrong'");
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    public void countTest() {
        assertThrows(IllegalArgumentException.class, () -> userSearchService.count((String) null));
        assertThrows(IllegalArgumentException.class, () -> userSearchService.count("process: identifier eq 'query_lang_test'"));

        long result = userSearchService.count("users: email eq '" + superCreator.getSuperUser().getEmail() + "'");
        assertEquals(1, result);

        result = userSearchService.count("users: email eq 'wrong'");
        assertEquals(0, result);
    }

    @Test
    public void existsTest() {
        assertThrows(IllegalArgumentException.class, () -> userSearchService.exists((String) null));
        assertThrows(IllegalArgumentException.class, () -> userSearchService.exists("process: identifier eq 'query_lang_test'"));

        boolean result = userSearchService.exists("users: email eq '" + superCreator.getSuperUser().getEmail() + "'");
        assertTrue(result);

        result = userSearchService.exists("users: email eq 'wrong'");
        assertFalse(result);
    }
}
