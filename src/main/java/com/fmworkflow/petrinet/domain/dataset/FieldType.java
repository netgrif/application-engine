package com.fmworkflow.petrinet.domain.dataset;

public enum FieldType {
    TEXT ("text"),
    DATE ("date"),
    BOOLEAN ("boolean");

    String name;

    FieldType(String name) {
        this.name = name;
    }

    public static FieldType fromString(String name) {
        return FieldType.valueOf(name.toUpperCase());
    }
}
