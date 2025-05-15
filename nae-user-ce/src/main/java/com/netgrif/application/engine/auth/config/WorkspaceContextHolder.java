package com.netgrif.application.engine.auth.config;

import org.springframework.core.NamedInheritableThreadLocal;
import org.springframework.core.NamedThreadLocal;

public class WorkspaceContextHolder {

    private static final ThreadLocal<String> workspaceHolder =
            new NamedThreadLocal<String>("WorkspaceId Context");

    private static final ThreadLocal<String> inheritableWorkspaceHolder =
            new NamedInheritableThreadLocal<String>("WorkspaceId Context");

    /**
     * Reset the workspaceId for the current thread.
     */
    public static void resetWorkspaceId() {
        workspaceHolder.remove();
    }

    /**
     * Bind the given workspaceId to the current thread,
     * <i>not</i> exposing it as inheritable for child threads.
     * @param workspaceId the workspaceId to expose
     * @see #setWorkspaceId(String, boolean)
     */
    public static void setWorkspaceId(String workspaceId) {
        setWorkspaceId(workspaceId, false);
    }

    /**
     * Bind the given workspaceId to the current thread.
     * @param workspaceId the workspaceId to expose,
     * or {@code null} to reset the thread-bound context
     * @param inheritable whether to expose the workspaceId as inheritable
     * for child threads (using an {@link InheritableThreadLocal})
     */
    public static void setWorkspaceId(String workspaceId, boolean inheritable) {
        if (workspaceId == null) {
            resetWorkspaceId();
        }
        else {
            if (inheritable) {
                inheritableWorkspaceHolder.set(workspaceId);
                workspaceHolder.remove();
            }
            else {
                workspaceHolder.set(workspaceId);
                inheritableWorkspaceHolder.remove();
            }
        }
    }

    /**
     * Return the workspaceId currently bound to the thread.
     * @return the workspaceId currently bound to the thread,
     * or {@code null} if none bound
     */
    public static String getWorkspaceId() {
        String workspaceId = workspaceHolder.get();
        if (workspaceId == null) {
            workspaceId = inheritableWorkspaceHolder.get();
        }
        return workspaceId;
    }

}
