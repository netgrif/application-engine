package com.netgrif.application.engine.integration.plugins.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Slf4j
@Component
@PropertySource(value = "classpath:plugin.yaml", factory = PluginPropertySourceFactory.class)
@ConfigurationProperties(prefix = "nae.plugins")
public final class PluginProperties {

    private String[] restrictedPackages = new String[]{"java.io","org.springframework.data","com.netgrif.workflow"};

    private String dir = "plugins";

    private String[] pluginFileNames = new String[]{"*"};

    private List<Permission> permissions = new ArrayList<>();

    public void setPermissions(Map<Class<?>, Object[]> permissionsMap) {
        permissionsMap.forEach((k, v) -> {
            Permission permission = createPermissionFromClass(k, v);
            if (permission != null)
                this.permissions.add(permission);
        });
    }

    private Permission createPermissionFromClass(Class<?> permClass, Object... args) {
        Permission permission = null;

        if (!permClass.getPackageName().contains("java.security") && !Permission.class.isAssignableFrom(permClass)) {
            log.error("Provided permission class is not from the official java.security package or it does not extends" +
                    "from abstract java.security.Permission class.");
            return null;
        }

        try {
            Class<?>[] argTypes = new Class[args.length];
            Arrays.stream(args).map(Object::getClass).toList().toArray(argTypes);
            permission = (Permission) permClass.getDeclaredConstructor(argTypes).newInstance(args);
        } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("Cannot create an instance of type [" + permClass + "]", e);
        }
        return permission;
    }
}
