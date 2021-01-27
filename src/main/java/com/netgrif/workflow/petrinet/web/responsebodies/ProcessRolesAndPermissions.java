package com.netgrif.workflow.petrinet.web.responsebodies;

import lombok.Data;

import java.util.*;

@Data
public class ProcessRolesAndPermissions {

    private Collection<ProcessRole> processRoles;

    private Map<String, Map<String, Boolean>> permissions;

    public ProcessRolesAndPermissions(Collection<ProcessRole> processRoles, Map<String, Map<String, Boolean>> permissions) {
        this.permissions = new HashMap<>();
        this.processRoles = processRoles;
        this.permissions.putAll(permissions);
    }

    public ProcessRolesAndPermissions() {
        this.permissions = new HashMap<>();
    }
}
