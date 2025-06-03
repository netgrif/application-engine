package com.netgrif.application.engine;

import com.netgrif.application.engine.auth.config.WorkspaceContextHolder;
import com.netgrif.application.engine.auth.service.UserService;
import groovy.lang.Closure;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncRunnerWrapper {
    private final AsyncRunner async;
    private final UserService userService;

    public void run(Closure closure) {
        run(closure, userService.getLoggedOrSystem().getWorkspaceId());
    }

    public void run(Closure closure, String workspaceId) {
        WorkspaceContextHolder.setWorkspaceId(workspaceId, true);
        async.run(closure);
    }

    public void execute(final Runnable runnable) {
        execute(runnable, userService.getLoggedOrSystem().getWorkspaceId());
    }

    public void execute(final Runnable runnable, String workspaceId) {
        WorkspaceContextHolder.setWorkspaceId(workspaceId, true);
        async.execute(runnable);
    }
}
