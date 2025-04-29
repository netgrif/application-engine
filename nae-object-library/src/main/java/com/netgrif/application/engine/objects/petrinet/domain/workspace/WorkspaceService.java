package com.netgrif.application.engine.objects.petrinet.domain.workspace;

import java.util.List;
import java.util.Optional;

public interface WorkspaceService {

    Workspace save(Workspace workspace);

    Optional<Workspace> get(String id);

    List<Workspace> getAll();

    Workspace createDefaultWorkspace();

    Workspace setDefaultWorkspace(String workspaceId);

    Workspace setDefaultWorkspace(Workspace workspace);

    Workspace getDefaultWorkspace();

    void delete(String identifier);
}
