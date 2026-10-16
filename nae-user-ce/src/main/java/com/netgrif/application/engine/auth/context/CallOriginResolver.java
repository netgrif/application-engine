package com.netgrif.application.engine.auth.context;

import org.springframework.stereotype.Component;

/**
 * Spring component used to resolve the origin of the current execution context.
 * <p>
 * Evaluates whether the execution was triggered by an incoming HTTP request,
 * a scheduled task, or application startup by inspecting {@link ExecutionContext}.
 */
@Component
public class CallOriginResolver {

    /**
     * Checks if the current execution origin is an incoming HTTP request.
     *
     * @return {@code true} if the current execution context originated from a request, {@code false} otherwise
     */
    public boolean isRequest() {
        return ExecutionContext.get() == Origin.REQUEST;
    }

    /**
     * Checks if the current execution origin is a scheduled task execution.
     *
     * @return {@code true} if the current execution context originated from a scheduled task, {@code false} otherwise
     */
    public boolean isScheduled() {
        return ExecutionContext.get() == Origin.SCHEDULED;
    }

    /**
     * Checks if the current execution origin is application startup.
     *
     * @return {@code true} if the current execution context originated from application startup, {@code false} otherwise
     */
    public boolean isStartup() {
        return ExecutionContext.get() == Origin.STARTUP;
    }
}
