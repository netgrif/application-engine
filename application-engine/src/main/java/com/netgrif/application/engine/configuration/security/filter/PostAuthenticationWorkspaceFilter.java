package com.netgrif.application.engine.configuration.security.filter;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.auth.domain.enums.WorkspacePermission;
import com.netgrif.application.engine.objects.workspace.Workspace;
import com.netgrif.application.engine.workspace.service.WorkspaceService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import static com.netgrif.application.engine.objects.workspace.WorkspaceConstants.WORKSPACE_ID_HEADER;

@Slf4j
@RequiredArgsConstructor
public class PostAuthenticationWorkspaceFilter extends OncePerRequestFilter {

    private final WorkspaceService workspaceService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoggedUser loggedUser) {
            Optional<Workspace> workspaceOptional = findWorkspaceInHeaders(request);
            Workspace workspace = workspaceOptional.orElse(workspaceService.getDefault());
            if (loggedUser.isAdmin() || loggedUser.hasWorkspacePermission(workspace.getId(), WorkspacePermission.READ_WRITE)) {
                loggedUser.setActiveWorkspaceId(workspace.getId());
            }
        }
        filterChain.doFilter(request, response);
    }

    private Optional<Workspace> findWorkspaceInHeaders(HttpServletRequest request) {
        String workspaceId = request.getHeader(WORKSPACE_ID_HEADER);
        if (workspaceId == null || workspaceId.isBlank()) {
            return Optional.empty();
        }
        return workspaceService.findOne(workspaceId);
    }
}
