package com.netgrif.application.engine.petrinet.domain;

public enum FunctionScope {

    NAMESPACE("namespace"),
    PROCESS("process");

    private final String value;

    FunctionScope(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FunctionScope fromValue(String v) {
        for (FunctionScope c : FunctionScope.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
