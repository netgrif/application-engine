package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;

import java.util.HashMap;
import java.util.HashSet;
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

    protected Map<String, Boolean> findUserPermissions(Map<String, Map<String, Boolean>> docPermissions, AbstractUser user) {
        // TODO: impersonation
        if (docPermissions == null) {
            return null;
        }
        Map<String, Boolean> permissions = docPermissions.get(user.getStringId());
        if (user.getGroupIds().isEmpty()) {
            return permissions;
        } else {
            // Defensive copy: do not mutate docPermissions' backing maps
            permissions = (permissions == null) ? null : new HashMap<>(permissions);
            Set<String> intersectionOfActorIds = new HashSet<>(docPermissions.keySet());
            intersectionOfActorIds.retainAll(user.getGroupIds());

            if (intersectionOfActorIds.isEmpty()) {
                return permissions;
            }

            if (permissions == null) {
                permissions = new HashMap<>();
            }

            for (String actorId : intersectionOfActorIds) {
                putPermissionIfNotAlreadyNegative(permissions, docPermissions.get(actorId));
            }

            return permissions;
        }
    }

    protected static void putPermissionIfNotAlreadyNegative(Map<String, Boolean> permissions, Map<String, Boolean> newPermissions) {
        if (newPermissions == null) {
            return;
        }
        for (Map.Entry<String, Boolean> entry : newPermissions.entrySet()) {
            putPermissionIfNotAlreadyNegative(permissions, entry.getKey(), entry.getValue());
        }
    }

    protected static void putPermissionIfNotAlreadyNegative(Map<String, Boolean> permissions, String permType, Boolean permValue) {
        if (permValue == null) {
            return;
        }
        Boolean existingPermValue = permissions.get(permType);
        // Overwrite if no existing value OR existing is positive (true).
        // Existing negative (false) permissions are never overwritten to preserve explicit denials.
        if (existingPermValue == null || existingPermValue) {
            permissions.put(permType, permValue);
        }
    }
}
