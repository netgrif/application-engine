package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.petrinet.domain.roles.CasePermission;
import com.netgrif.application.engine.petrinet.domain.roles.TaskPermission;

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

    protected Map<CasePermission, Boolean> getAggregateProcessRolePermissions(IUser user, Map<String, Map<CasePermission, Boolean>> permissions) {
        Map<CasePermission, Boolean> aggregatePermissions = new HashMap<>();

        Set<String> userProcessRoleIDs = user.getSelfOrImpersonated().getProcessRoles().stream().map(role -> role.getId().toString()).collect(Collectors.toSet());

        for (Map.Entry<String, Map<CasePermission, Boolean>> role : permissions.entrySet()) {
            aggregateProcessRolePermission(userProcessRoleIDs, role, aggregatePermissions);
        }

        return aggregatePermissions;
    }

    private void aggregateProcessRolePermission(Set<String> userProcessRoleIDs, Map.Entry<String, Map<CasePermission, Boolean>> role, Map<CasePermission, Boolean> aggregatePermissions) {
        if (!userProcessRoleIDs.contains(role.getKey())) {
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

        Set<String> userProcessRoleIDs = user.getSelfOrImpersonated().getProcessRoles().stream().map(role -> role.getId().toString()).collect(Collectors.toSet());

        for (Map.Entry<String, Map<TaskPermission, Boolean>> role : permissions.entrySet()) {
            aggregateRolePermission(userProcessRoleIDs, role, aggregatePermissions);
        }

        return aggregatePermissions;
    }

    private void aggregateRolePermission(Set<String> userProcessRoleIDs, Map.Entry<String, Map<TaskPermission, Boolean>> role, Map<TaskPermission, Boolean> aggregatePermissions) {
        if (!userProcessRoleIDs.contains(role.getKey())) {
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
