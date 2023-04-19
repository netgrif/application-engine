package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
class AbstractAuthorizationServiceTest {

    static class MockAuthorizationService extends AbstractAuthorizationService { }

    @Test
    public void hasPermission() {
        MockAuthorizationService mockInstance = new MockAuthorizationService();
        assert mockInstance.hasPermission(Boolean.TRUE);
        assert !mockInstance.hasPermission(Boolean.FALSE);
        assert !mockInstance.hasPermission(null);
    }

    @Test
    public void hasRestrictedPermission() {
        MockAuthorizationService mockInstance = new MockAuthorizationService();
        assert !mockInstance.hasRestrictedPermission(Boolean.TRUE);
        assert mockInstance.hasRestrictedPermission(Boolean.FALSE);
        assert !mockInstance.hasRestrictedPermission(null);
    }

    @Test
    public void getAggregatePermissions() {
        MockAuthorizationService mockInstance = new MockAuthorizationService();

        // init
        List<ProcessRole> roles = new LinkedList<>();
        roles.add(new ProcessRole());
        roles.add(new ProcessRole());
        roles.add(new ProcessRole());

        IUser user = new User();
        user.addProcessRole(roles.get(0));
        user.addProcessRole(roles.get(1));

        Map<String, Map<String, Boolean>> netPermissions = new HashMap<>();
        netPermissions.put(roles.get(0).getStringId(), getInitEntryValue());
        netPermissions.put(roles.get(1).getStringId(), getInitEntryValue());
        netPermissions.put(roles.get(2).getStringId(), getInitEntryValue());

        // situation 1
        Map<String, Boolean> aggregatePermission = mockInstance.getAggregatePermissions(user, netPermissions);

        assert aggregatePermission.get("create");
        assert aggregatePermission.get("view");
        assert aggregatePermission.get("delete");

        // situation 2
        netPermissions.get(roles.get(0).getStringId()).put("create", false);
        netPermissions.get(roles.get(1).getStringId()).put("delete", false);
        aggregatePermission = mockInstance.getAggregatePermissions(user, netPermissions);

        assert !aggregatePermission.get("create");
        assert aggregatePermission.get("view");
        assert !aggregatePermission.get("delete");
    }

    private Map<String, Boolean> getInitEntryValue() {
        Map<String, Boolean> result = new HashMap<>();
        result.put("create", true);
        result.put("view", true);
        result.put("delete", true);
        return result;
    }
}