package com.netgrif.application.engine.objects.petrinet.domain.workspace;

public interface DefaultWorkspaceService {

    public static final String DEFAULT_WORKSPACE_ID = "default";

    Workspace getDefaultWorkspace();
}
