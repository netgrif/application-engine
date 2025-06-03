package com.netgrif.application.engine.objects.petrinet.domain.workspace;

public record WorkspaceRequest(
        String workspaceId,
        String name,
        String description,
        boolean defaultWorkspace) {
}

