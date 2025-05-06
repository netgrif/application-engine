package com.netgrif.application.engine.objects.petrinet.domain.workspace;

import java.util.List;

public interface DefaultWorkspaceService {

    public static final String DEFAULT_WORKSPACE_ID = "default";

    Workspace getDefaultWorkspace();

    List<Workspace> getAllWorkspaces();
}
