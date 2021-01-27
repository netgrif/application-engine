package com.netgrif.workflow.petrinet.web.responsebodies;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ProcessPermissions {

    private Map<String, Map<String, Boolean>> permissions;

    public ProcessPermissions(Map<String, Map<String, Boolean>> permissions) {
        this.permissions = new HashMap<>();
        this.permissions.putAll(permissions);
    }

    public ProcessPermissions() {
        this.permissions = new HashMap<>();
    }
}
