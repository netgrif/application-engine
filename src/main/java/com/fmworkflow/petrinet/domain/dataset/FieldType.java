package com.fmworkflow.petrinet.domain.dataset;

import com.fasterxml.jackson.annotation.JsonValue;

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

    @JsonValue
    public String getName() {
        return name;
    }
}
