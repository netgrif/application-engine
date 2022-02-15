package com.netgrif.application.engine.integration.plugins.security;

import lombok.Setter;
import org.pf4j.PluginClassLoader;

import java.security.*;
import java.util.ArrayList;
import java.util.List;

class PluginSecurityPolicy extends Policy {

    @Setter
    private List<Permission> permissionList;

    protected PluginSecurityPolicy() {
        this.permissionList = new ArrayList<>();
    }

    protected PluginSecurityPolicy(List<Permission> permissionList) {
        this();
        this.permissionList = permissionList;
    }

    @Override
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        Permissions permissions = new Permissions();

        if (domain.getClassLoader() instanceof PluginClassLoader) {
            permissionList.forEach(permissions::add);
        }

        return permissions;
    }
}
