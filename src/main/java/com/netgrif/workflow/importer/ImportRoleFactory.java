package com.netgrif.workflow.importer;


import com.netgrif.workflow.importer.model.RoleLogic;
import com.netgrif.workflow.petrinet.domain.roles.RolePermission;

import java.util.HashSet;
import java.util.Set;

public class ImportRoleFactory {

    public ImportRoleFactory() {
    }

    public static Set<RolePermission> getPermissions(RoleLogic roleLogic){
        Set<RolePermission> permissions = new HashSet<>();

        if(roleLogic.getPerform()) permissions.add(RolePermission.PERFORM);
        if(roleLogic.getDelegate()) permissions.add(RolePermission.DELEGATE);

        return permissions;
    }
}
