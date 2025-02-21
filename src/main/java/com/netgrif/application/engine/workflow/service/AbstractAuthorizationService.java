package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.authorization.domain.permissions.TaskPermission;

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

    protected Map<CasePermission, Boolean> getAggregateRoleCasePermissions(IUser user, Map<String, Map<CasePermission, Boolean>> permissions) {
        Map<CasePermission, Boolean> aggregatePermissions = new HashMap<>();

        // todo 2058
//        Set<String> userRoleIDs = user.getSelfOrImpersonated().getRoles().stream().map(role -> role.getId().toString()).collect(Collectors.toSet());

        for (Map.Entry<String, Map<CasePermission, Boolean>> role : permissions.entrySet()) {
//            aggregateRoleCasePermission(userRoleIDs, role, aggregatePermissions);
        }

        return aggregatePermissions;
    }

    private void aggregateRoleCasePermission(Set<String> userRoleIDs, Map.Entry<String, Map<CasePermission, Boolean>> role, Map<CasePermission, Boolean> aggregatePermissions) {
        if (!userRoleIDs.contains(role.getKey())) {
            return;
        }
        for (Map.Entry<CasePermission, Boolean> permission : role.getValue().entrySet()) {
            Boolean permissionValue = permission.getValue();
            if (aggregatePermissions.containsKey(permission.getKey())) {
                permissionValue = aggregatePermissions.get(permission.getKey()) && permissionValue;
            }
            aggregatePermissions.put(permission.getKey(), permissionValue);
        }
    }

    protected Map<TaskPermission, Boolean> getAggregateRolePermissions(IUser user, Map<String, Map<TaskPermission, Boolean>> permissions) {
        Map<TaskPermission, Boolean> aggregatePermissions = new HashMap<>();

        // todo 2058
//        Set<String> userRoleIDs = user.getSelfOrImpersonated().getRoles().stream().map(role -> role.getId().toString()).collect(Collectors.toSet());

        for (Map.Entry<String, Map<TaskPermission, Boolean>> role : permissions.entrySet()) {
//            aggregateRolePermission(userRoleIDs, role, aggregatePermissions);
        }

        return aggregatePermissions;
    }

    private void aggregateRolePermission(Set<String> userRoleIDs, Map.Entry<String, Map<TaskPermission, Boolean>> role, Map<TaskPermission, Boolean> aggregatePermissions) {
        if (!userRoleIDs.contains(role.getKey())) {
            return;
        }
        for (Map.Entry<TaskPermission, Boolean> permission : role.getValue().entrySet()) {
            Boolean permissionValue = permission.getValue();
            if (aggregatePermissions.containsKey(permission.getKey())) {
                permissionValue = aggregatePermissions.get(permission.getKey()) && permissionValue;
            }
            aggregatePermissions.put(permission.getKey(), permissionValue);
        }
    }
}
