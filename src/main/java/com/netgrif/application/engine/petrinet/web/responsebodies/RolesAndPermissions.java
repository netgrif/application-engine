package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.authorization.domain.ProcessRole;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RolesAndPermissions {

    private List<ProcessRole> processRoles;

    private Map<String, Map<CasePermission, Boolean>> permissions;

    public RolesAndPermissions(List<ProcessRole> processRoles, Map<String, Map<CasePermission, Boolean>> permissions) {
        this();
        this.processRoles = processRoles;
        this.permissions.putAll(permissions);
    }

    public RolesAndPermissions() {
        this.processRoles = new ArrayList<>();
        this.permissions = new HashMap<>();
    }
}
