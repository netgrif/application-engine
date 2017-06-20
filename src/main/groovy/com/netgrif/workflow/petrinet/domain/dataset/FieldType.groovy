package com.netgrif.workflow.petrinet.domain.dataset;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FieldType {
    TEXT ("text"),
    DATE ("date"),
    BOOLEAN ("boolean"),
    FILE ("file"),
    ENUMERATION ("enumeration"),
    MULTICHOICE ("multichoice"),
    NUMBER("number"),
    USER("user"),
    TABULAR("tabular");

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
