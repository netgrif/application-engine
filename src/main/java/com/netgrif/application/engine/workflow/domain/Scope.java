package com.netgrif.application.engine.workflow.domain;

/**
 * todo javadoc
 * */
public enum Scope {

    USECASE("useCase"),
    PROCESS("process"),
    APPLICATION("application"),
    NAMESPACE("namespace");

    private final String value;

    Scope(String v) {
        value = v;
    }

    public static Scope fromValue(String v) {
        for (Scope c : Scope.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public String value() {
        return value;
    }
}
