package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.objects.dto.response.petrinet.ProcessRoleDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ProcessRolesAndPermissions {

    private List<ProcessRoleDto> processRoles;

    private Map<String, Map<String, Boolean>> permissions;

    public ProcessRolesAndPermissions(List<ProcessRoleDto> processRoles, Map<String, Map<String, Boolean>> permissions) {
        this();
        this.processRoles = processRoles;
        this.permissions.putAll(permissions);
    }

    public ProcessRolesAndPermissions() {
        this.processRoles = new ArrayList<>();
        this.permissions = new HashMap<>();
    }
}
