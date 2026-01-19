package com.netgrif.application.engine.workspace.service;

import com.netgrif.application.engine.objects.workspace.Workspace;
import org.springframework.stereotype.Service;

import static com.netgrif.application.engine.objects.workspace.DefaultWorkspaceConstants.*;

// todo javadoc
@Service
public class MockWorkspaceService implements WorkspaceService {

    private final Workspace defaultWorkspace;

    public MockWorkspaceService() {
        this.defaultWorkspace = new com.netgrif.application.engine.adapter.spring.workspace.Workspace(DEFAULT_WORKSPACE_ID);
        this.defaultWorkspace.setName(DEFAULT_WORKSPACE_NAME);
        this.defaultWorkspace.setDescription(DEFAULT_WORKSPACE_DESC);
        this.defaultWorkspace.setDefaultWorkspace(true);
    }

    @Override
    public Workspace getDefault() {
        return this.defaultWorkspace;
    }

    @Override
    public Workspace findOne(String workspaceId) {
        if (workspaceId == null) {
            return null;
        }

        if (workspaceId.equals(DEFAULT_WORKSPACE_ID)) {
            return this.defaultWorkspace;
        }

        return null;
    }
}
