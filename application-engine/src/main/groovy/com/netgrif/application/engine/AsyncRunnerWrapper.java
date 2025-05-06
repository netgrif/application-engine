package com.netgrif.application.engine;

import com.netgrif.application.engine.petrinet.service.workspace.WorkspaceContextHolder;
import groovy.lang.Closure;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncRunnerWrapper {
    private final AsyncRunner async;

    public void run(Closure closure, String workspaceId) {
        WorkspaceContextHolder.setWorkspaceId(workspaceId, true);
        async.run(closure);
    }

    public void execute(final Runnable runnable, String workspaceId) {
        WorkspaceContextHolder.setWorkspaceId(workspaceId, true);
        async.execute(runnable);
    }
}
