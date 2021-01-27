package com.netgrif.workflow.petrinet.web.responsebodies;

import lombok.Data;

import java.util.*;

@Data
public class ProcessRolesAndPermissions {

    private List<ProcessRole> processRoles;

    private Map<String, Map<String, Boolean>> permissions;

    public ProcessRolesAndPermissions(List<ProcessRole> processRoles, Map<String, Map<String, Boolean>> permissions) {
        this();
        this.processRoles = processRoles;
        this.permissions.putAll(permissions);
    }

    public ProcessRolesAndPermissions() {
        this.processRoles = new ArrayList<>();
        this.permissions = new HashMap<>();
    }
}
