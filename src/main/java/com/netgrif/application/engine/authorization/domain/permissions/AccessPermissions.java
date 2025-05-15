package com.netgrif.application.engine.authorization.domain.permissions;

import lombok.Data;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Data
public class AccessPermissions<T> {

    private Map<String, Map<T, Boolean>> permissions;

    public AccessPermissions() {
        this.permissions = new HashMap<>();
    }

    /**
     * todo javadoc
     * */
    public AccessPermissions(AccessPermissions<T> accessPermissions) {
        this(accessPermissions, new HashSet<>());
    }

    /**
     * todo javadoc
     * */
    public AccessPermissions(AccessPermissions<T> accessPermissions, Set<T> ignoreTypes) {
        accessPermissions.forEach((roleId, permissionValues) -> {
            Map<T, Boolean> clonedPermissionValues;
            if (ignoreTypes.isEmpty()) {
                clonedPermissionValues = new HashMap<>(permissionValues);
            } else {
                clonedPermissionValues = permissionValues.entrySet().stream()
                        .filter((permEntry) -> !ignoreTypes.contains(permEntry.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            }
            this.addPermissions(roleId, clonedPermissionValues);
        });
    }

    public void forEach(BiConsumer<String, Map<T, Boolean>> action) {
        this.permissions.forEach(action);
    }

    public boolean containsKey(String roleId) {
        return this.permissions.containsKey(roleId);
    }

    public Map<T, Boolean> get(String roleId) {
        return this.permissions.get(roleId);
    }

    public void put(String roleId, Map<T, Boolean> permissions) {
        this.permissions.put(roleId, permissions);
    }

    public boolean isEmpty() {
        return this.permissions.isEmpty();
    }

    /**
     * todo javadoc
     * */
    public void addPermission(String roleId, T permission, Boolean value) {
        Map<T, Boolean> permissionEntry = new HashMap<>();
        permissionEntry.put(permission, value);
        // permissionEntry must be mutable
        this.addPermissions(roleId, permissionEntry);
    }

    /**
     * todo javadoc
     * */
    public void addPermissions(String roleId, Map<T, Boolean> permissions) {
        if (this.containsKey(roleId) && this.get(roleId) != null) {
            this.get(roleId).putAll(permissions);
        } else {
            this.put(roleId, permissions);
        }
    }

    /**
     * todo javadoc
     * */
    public void addPermissions(AccessPermissions<T> rolesAndPermissions) {
        if (rolesAndPermissions == null) {
            return;
        }
        rolesAndPermissions.forEach(this::addPermissions);
    }
}
