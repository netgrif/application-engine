package com.netgrif.application.engine.petrinet.domain.roles;

import com.netgrif.application.engine.utils.UniqueKeyMap;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
public class RolePermissions<T> extends UniqueKeyMap<String, Map<T, Boolean>> {

    /**
     * todo javadoc
     * */
    public RolePermissions(RolePermissions<T> rolePermissions) {
        this(rolePermissions, new HashSet<>());
    }

    /**
     * todo javadoc
     * */
    public RolePermissions(RolePermissions<T> rolePermissions, Set<T> ignoreTypes) {
        rolePermissions.forEach((roleId, permissionValues) -> {
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

    /**
     * todo javadoc
     * */
    public void addPermission(String roleId, T permission, Boolean value) {
        Map<T, Boolean> permissionEntry = new HashMap<>();
        permissionEntry.put(permission, value);
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
}
