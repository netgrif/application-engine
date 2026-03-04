package com.netgrif.application.engine.workspace.service;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.auth.domain.enums.WorkspacePermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspacePermissionService {

    private final WorkspaceService workspaceService;

    // todo javadoc
    public boolean checkPermissionAndSelectWorkspace(String workspaceId, LoggedUser loggedUser) {
        if (workspaceId == null || workspaceId.isEmpty()) {
            workspaceId = workspaceService.getDefault().getId();
        }
        boolean hasPermission = loggedUser != null
                && (loggedUser.isAdmin() || loggedUser.hasWorkspacePermission(workspaceId, WorkspacePermission.READ_WRITE));
        if (hasPermission) {
            loggedUser.setActiveWorkspaceId(workspaceId);
        } else if (loggedUser != null) {
            log.warn("User [{}] tried to access workspace [{}] without permission", loggedUser.getUsername(), workspaceId);
        }
        return hasPermission;
    }
}
