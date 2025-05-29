package com.netgrif.application.engine.petrinet.domain.layout;

public enum LayoutObjectType {
    FLEX("flex"),
    GRID("grid");

    private final String value;

    LayoutObjectType(String v) {
        value = v;
    }

    public static LayoutObjectType fromValue(String v) {
        for (LayoutObjectType c : LayoutObjectType.values()) {
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
