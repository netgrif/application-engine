package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.authentication.domain.IUser;
import com.netgrif.application.engine.authentication.domain.User;
import com.netgrif.application.engine.authorization.domain.ProcessRole;
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.netgrif.application.engine.authorization.domain.permissions.CasePermission.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
class AbstractAuthorizationServiceTest {

    static class MockAuthorizationService extends AbstractAuthorizationService {
    }

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
        List<ProcessRole> processRoles = new LinkedList<>();
        processRoles.add(new ProcessRole());
        processRoles.add(new ProcessRole());
        processRoles.add(new ProcessRole());

        IUser user = new User();
//        todo 2058
//        user.addRole(processRoles.get(0));
//        user.addRole(processRoles.get(1));

        Map<String, Map<CasePermission, Boolean>> netPermissions = new HashMap<>();
        netPermissions.put(processRoles.get(0).getStringId(), getInitEntryValue());
        netPermissions.put(processRoles.get(1).getStringId(), getInitEntryValue());
        netPermissions.put(processRoles.get(2).getStringId(), getInitEntryValue());

        // situation 1
        Map<CasePermission, Boolean> aggregatePermission = mockInstance.getAggregateRoleCasePermissions(user, netPermissions);

        assert aggregatePermission.get(CREATE);
        assert aggregatePermission.get(VIEW);
        assert aggregatePermission.get(DELETE);

        // situation 2
        netPermissions.get(processRoles.get(0).getStringId()).put(CREATE, false);
        netPermissions.get(processRoles.get(1).getStringId()).put(DELETE, false);
        aggregatePermission = mockInstance.getAggregateRoleCasePermissions(user, netPermissions);

        // TODO: release/8.0.0 AssertionError
        assert !aggregatePermission.get(CREATE);
        assert aggregatePermission.get(VIEW);
        assert !aggregatePermission.get(DELETE);
    }

    private Map<CasePermission, Boolean> getInitEntryValue() {
        Map<CasePermission, Boolean> result = new HashMap<>();
        result.put(CREATE, true);
        result.put(VIEW, true);
        result.put(DELETE, true);
        return result;
    }
}