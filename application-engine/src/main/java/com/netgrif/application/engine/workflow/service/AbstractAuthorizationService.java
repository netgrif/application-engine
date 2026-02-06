package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractAuthorizationService {

    protected boolean hasPermission(Boolean permissionValue) {
        return permissionValue != null && permissionValue;
    }

    protected boolean hasRestrictedPermission(Boolean permissionValue) {
        return permissionValue != null && !permissionValue;
    }

    protected Map<String, Boolean> getAggregatePermissions(AbstractUser user, Map<String, Map<String, Boolean>> permissions) {
        Map<String, Boolean> aggregatePermissions = new HashMap<>();

//        Set<String> userProcessRoleIDs = user.getSelfOrImpersonated().getProcessRoles().stream().map(role -> role.get_id().toString()).collect(Collectors.toSet());
        // TODO: impersonation
        Set<String> userProcessRoleIDs = user.getProcessRoles().stream()
                .map(role -> role.get_id().toString())
                .collect(Collectors.toSet());

        for (Map.Entry<String, Map<String, Boolean>> role : permissions.entrySet()) {
            aggregatePermission(userProcessRoleIDs, role, aggregatePermissions);
        }

        return aggregatePermissions;
    }

    private void aggregatePermission(Set<String> userProcessRoleIDs, Map.Entry<String, Map<String, Boolean>> role,
                                     Map<String, Boolean> aggregatePermissions) {
        if (!userProcessRoleIDs.contains(role.getKey())) {
            return;
        }
        for (Map.Entry<String, Boolean> permission : role.getValue().entrySet()) {
            if (aggregatePermissions.containsKey(permission.getKey())) {
                aggregatePermissions.put(permission.getKey(), aggregatePermissions.get(permission.getKey()) && permission.getValue());
            } else {
                aggregatePermissions.put(permission.getKey(), permission.getValue());
            }
        }
    }
}
