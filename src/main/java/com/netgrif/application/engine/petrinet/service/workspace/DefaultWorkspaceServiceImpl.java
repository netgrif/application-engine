package com.netgrif.application.engine.petrinet.service.workspace;

import com.netgrif.core.petrinet.domain.workspace.DefaultWorkspaceService;
import com.netgrif.core.petrinet.domain.workspace.Workspace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
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
}
