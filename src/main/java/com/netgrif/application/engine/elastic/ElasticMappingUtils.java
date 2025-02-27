package com.netgrif.application.engine.elastic;

import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ElasticMappingUtils {

    /**
     * todo javadoc
     * */
    public static <T> Set<String> filterRoleIdsByPermissionValue(AccessPermissions<T> permissions, T permission, boolean isPositive) {
        return filterRoleIds(permissions, (permissionEntry) -> permissionEntry.getValue().get(permission) == isPositive);
    }

    /**
     * todo javadoc
     * */
    public static <T> Set<String> filterRoleIdsByPermissionType(AccessPermissions<T> permissions, T permission) {
        return filterRoleIds(permissions, (permissionEntry) -> permissionEntry.getValue().containsKey(permission));
    }

    protected static <T> Set<String> filterRoleIds(AccessPermissions<T> permissions, Predicate<Map.Entry<String, Map<T, Boolean>>> predicate) {
        return permissions.entrySet().stream()
                .filter(predicate)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
