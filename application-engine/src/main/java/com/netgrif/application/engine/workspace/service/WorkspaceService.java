package com.netgrif.application.engine.workspace.service;

import com.netgrif.application.engine.objects.workspace.Workspace;

import java.util.Optional;

public interface WorkspaceService {
    Workspace getDefault();
    Optional<Workspace> findOne(String workspaceId);
}
