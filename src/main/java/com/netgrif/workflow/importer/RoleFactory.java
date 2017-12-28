package com.netgrif.workflow.importer;


import com.netgrif.workflow.importer.model.Logic;
import com.netgrif.workflow.petrinet.domain.roles.RolePermission;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class RoleFactory {

    public Set<RolePermission> getPermissions(Logic roleLogic) {
        Set<RolePermission> permissions = new HashSet<>();

        addPerform(permissions, roleLogic);
        addDelegate(permissions, roleLogic);

        return permissions;
    }

    private void addPerform(Set<RolePermission> permissions, Logic roleLogic) {
        if (roleLogic.isPerform() != null && roleLogic.isPerform())
            permissions.add(RolePermission.PERFORM);
    }

    private void addDelegate(Set<RolePermission> permissions, Logic roleLogic) {
        if (roleLogic.isDelegate() != null && roleLogic.isDelegate())
            permissions.add(RolePermission.DELEGATE);
    }
}
