package com.netgrif.application.engine.auth.web.responsebodies;

import lombok.Data;

@Data
public class WorkspaceResponse {

    private String id;

    private boolean defaultWorkspace;

    public WorkspaceResponse(String id, boolean defaultWorkspace) {
        this.id = id;
        this.defaultWorkspace = defaultWorkspace;
    }
}
