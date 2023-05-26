package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.domain.IUser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractAuthorizationService {

    protected boolean hasPermission(Boolean hasPermission) {
        return hasPermission != null && hasPermission;
    }

    protected boolean hasRestrictedPermission(Boolean hasPermission) {
        return hasPermission != null && !hasPermission;
    }

    protected Map<String, Boolean> getAggregatePermissions(IUser user, Map<String, Map<String, Boolean>> permissions) {
        Map<String, Boolean> aggregatePermissions = new HashMap<>();

        Set<String> userProcessRoleIDs = user.getSelfOrImpersonated().getProcessRoles().stream().map(role -> role.get_id().toString()).collect(Collectors.toSet());

        for (Map.Entry<String, Map<String, Boolean>> role : permissions.entrySet()) {
            aggregatePermission(userProcessRoleIDs, role, aggregatePermissions);
        }

        return aggregatePermissions;
    }

    private void aggregatePermission(Set userProcessRoleIDs, Map.Entry<String, Map<String, Boolean>> role, Map<String, Boolean> aggregatePermissions) {
        if (userProcessRoleIDs.contains(role.getKey())) {
            for (Map.Entry<String, Boolean> permission : role.getValue().entrySet()) {
                if (aggregatePermissions.containsKey(permission.getKey())) {
                    aggregatePermissions.put(permission.getKey(), aggregatePermissions.get(permission.getKey()) && permission.getValue());
                } else {
                    aggregatePermissions.put(permission.getKey(), permission.getValue());
                }
            }
        }
    }
}
