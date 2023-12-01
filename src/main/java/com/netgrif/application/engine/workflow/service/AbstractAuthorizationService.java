package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRolePermission;
import com.netgrif.application.engine.petrinet.domain.roles.RolePermission;

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

    protected Map<ProcessRolePermission, Boolean> getAggregateProcessRolePermissions(IUser user, Map<String, Map<ProcessRolePermission, Boolean>> permissions) {
        Map<ProcessRolePermission, Boolean> aggregatePermissions = new HashMap<>();

        Set<String> userProcessRoleIDs = user.getSelfOrImpersonated().getProcessRoles().stream().map(role -> role.getId().toString()).collect(Collectors.toSet());

        for (Map.Entry<String, Map<ProcessRolePermission, Boolean>> role : permissions.entrySet()) {
            aggregateProcessRolePermission(userProcessRoleIDs, role, aggregatePermissions);
        }

        return aggregatePermissions;
    }

    private void aggregateProcessRolePermission(Set<String> userProcessRoleIDs, Map.Entry<String, Map<ProcessRolePermission, Boolean>> role, Map<ProcessRolePermission, Boolean> aggregatePermissions) {
        if (!userProcessRoleIDs.contains(role.getKey())) {
            return;
        }
        for (Map.Entry<ProcessRolePermission, Boolean> permission : role.getValue().entrySet()) {
            Boolean permissionValue = permission.getValue();
            if (aggregatePermissions.containsKey(permission.getKey())) {
                permissionValue = aggregatePermissions.get(permission.getKey()) || permissionValue;
            }
            aggregatePermissions.put(permission.getKey(), permissionValue);
        }
    }

    protected Map<RolePermission, Boolean> getAggregateRolePermissions(IUser user, Map<String, Map<RolePermission, Boolean>> permissions) {
        Map<RolePermission, Boolean> aggregatePermissions = new HashMap<>();

        Set<String> userProcessRoleIDs = user.getSelfOrImpersonated().getProcessRoles().stream().map(role -> role.getId().toString()).collect(Collectors.toSet());

        for (Map.Entry<String, Map<RolePermission, Boolean>> role : permissions.entrySet()) {
            aggregateRolePermission(userProcessRoleIDs, role, aggregatePermissions);
        }

        return aggregatePermissions;
    }

    private void aggregateRolePermission(Set<String> userProcessRoleIDs, Map.Entry<String, Map<RolePermission, Boolean>> role, Map<RolePermission, Boolean> aggregatePermissions) {
        if (!userProcessRoleIDs.contains(role.getKey())) {
            return;
        }
        for (Map.Entry<RolePermission, Boolean> permission : role.getValue().entrySet()) {
            Boolean permissionValue = permission.getValue();
            if (aggregatePermissions.containsKey(permission.getKey())) {
                permissionValue = aggregatePermissions.get(permission.getKey()) && permissionValue;
            }
            aggregatePermissions.put(permission.getKey(), permissionValue);
        }
    }
}
