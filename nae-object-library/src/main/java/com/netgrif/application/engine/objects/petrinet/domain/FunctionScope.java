package com.netgrif.application.engine.objects.petrinet.domain;

/**
 * Enum representing the scope of a function.
 * <p>
 * This enumeration defines the possible scopes in which a function can operate.
 */
public enum FunctionScope {

    /**
     * Represents the process-specific scope of a function.
     */
    PROCESS("process"),

    /**
     * Represents the global scope of a function.
     */
    GLOBAL("global");

    private final String value;

    FunctionScope(String v) {
        value = v;
    }

    public static FunctionScope fromValue(String v) {
        for (FunctionScope c : FunctionScope.values()) {
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
