package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.roles.ProcessRolePermission;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ProcessRolesAndPermissions {

    private List<ProcessRole> processRoles;

    private Map<String, Map<ProcessRolePermission, Boolean>> permissions;

    public ProcessRolesAndPermissions(List<ProcessRole> processRoles, Map<String, Map<ProcessRolePermission, Boolean>> permissions) {
        this();
        this.processRoles = processRoles;
        this.permissions.putAll(permissions);
    }

    public ProcessRolesAndPermissions() {
        this.processRoles = new ArrayList<>();
        this.permissions = new HashMap<>();
    }
}
