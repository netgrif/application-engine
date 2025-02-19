package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.roles.CasePermission;
import com.netgrif.application.engine.petrinet.domain.roles.Role;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RolesAndPermissions {

    private List<Role> roles;

    private Map<String, Map<CasePermission, Boolean>> permissions;

    public RolesAndPermissions(List<Role> roles, Map<String, Map<CasePermission, Boolean>> permissions) {
        this();
        this.roles = roles;
        this.permissions.putAll(permissions);
    }

    public RolesAndPermissions() {
        this.roles = new ArrayList<>();
        this.permissions = new HashMap<>();
    }
}
