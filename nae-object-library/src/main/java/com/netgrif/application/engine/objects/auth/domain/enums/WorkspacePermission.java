package com.netgrif.application.engine.objects.auth.domain.enums;

public enum WorkspacePermission {
    /** todo javadoc */
    READ("read"),
    READ_WRITE("read_write"),
    READ_WRITE_ADMIN("read_write_admin");

    private final String value;

    WorkspacePermission(String value) {
        this.value = value;
    }

    public static WorkspacePermission fromValue(String value) {
        for (WorkspacePermission wp : WorkspacePermission.values()) {
            if (wp.value.equals(value)) {
                return wp;
            }
        }
        throw new IllegalArgumentException(value);
    }
}
