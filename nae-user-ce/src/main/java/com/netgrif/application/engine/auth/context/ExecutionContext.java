package com.netgrif.application.engine.auth.context;

import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public final class ExecutionContext {

    private static final ThreadLocal<Origin> ORIGIN = new ThreadLocal<>();

    public static void set(Origin o) {
        ORIGIN.set(o);
    }

    public static Origin get() {
        return ORIGIN.get();
    }

    public static void clear() {
        ORIGIN.remove();
    }

    public static TaskDecorator getTaskDecorator() {
        return runnable -> {
            Origin currentOrigin = ExecutionContext.get();
            SecurityContext securityContext = SecurityContextHolder.getContext();
            return () -> {
                try {
                    ExecutionContext.set(currentOrigin != null ? currentOrigin : Origin.ASYNC_INTERNAL);
                    SecurityContextHolder.setContext(securityContext);
                    runnable.run();
                } finally {
                    ExecutionContext.clear();
                    SecurityContextHolder.clearContext();
                }
            };
        };
    }
}
