package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;

import java.util.Map;
import java.util.Set;

public abstract class AuthorizationService {

    /**
     * todo javadoc
     * */
    protected <T> boolean hasPositivePermission(Set<String> roleIds, AccessPermissions<T> permissions, T permission) {
        return hasPermission(roleIds, permissions, permission, true);
    }

    /**
     * todo javadoc
     * */
    protected <T> boolean hasNegativePermission(Set<String> roleIds, AccessPermissions<T> permissions, T permission) {
        return hasPermission(roleIds, permissions, permission, false);
    }

    // private on purpose
    private <T> boolean hasPermission(Set<String> roleIds, AccessPermissions<T> accessPermissions, T permission, Boolean value) {
        if (roleIds.isEmpty() || accessPermissions.isEmpty() || permission == null) {
            return false;
        }
        return accessPermissions.entrySet().stream().anyMatch((entry) -> {
            String roleId = entry.getKey();
            Map<T, Boolean> permissions = entry.getValue();

            return permissions.get(permission) == value && roleIds.contains(roleId);
        });
    }
}
