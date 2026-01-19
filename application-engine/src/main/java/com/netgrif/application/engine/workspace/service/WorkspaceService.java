package com.netgrif.application.engine.workspace.service;

import com.netgrif.application.engine.objects.workspace.Workspace;

public interface WorkspaceService {
    Workspace getDefault();
    Workspace findOne(String workspaceId);
}
