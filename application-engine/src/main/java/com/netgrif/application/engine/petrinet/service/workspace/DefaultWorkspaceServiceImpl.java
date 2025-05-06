package com.netgrif.application.engine.petrinet.service.workspace;

import com.netgrif.application.engine.objects.petrinet.domain.workspace.DefaultWorkspaceService;
import com.netgrif.application.engine.objects.petrinet.domain.workspace.Workspace;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DefaultWorkspaceServiceImpl implements DefaultWorkspaceService {

    private final Workspace defaultWorkspace;

    public DefaultWorkspaceServiceImpl() {
        this.defaultWorkspace = new Workspace();
        this.defaultWorkspace.setId(DefaultWorkspaceService.DEFAULT_WORKSPACE_ID);
        this.defaultWorkspace.setDefaultWorkspace(true);
    }

    @Override
    public Workspace getDefaultWorkspace() {
        return defaultWorkspace;
    }

    @Override
    public List<Workspace> getAllWorkspaces() {
        return List.of(defaultWorkspace);
    }
}
