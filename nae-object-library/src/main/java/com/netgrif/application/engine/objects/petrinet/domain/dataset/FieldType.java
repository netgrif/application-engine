package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum FieldType {
    TEXT("text"),
    DATE("date"),
    BOOLEAN("boolean"),
    FILE("file"),
    FILELIST("fileList"),
    ENUMERATION("enumeration"),
    ENUMERATION_MAP("enumeration_map"),
    MULTICHOICE("multichoice"),
    MULTICHOICE_MAP("multichoice_map"),
    NUMBER("number"),
    ACTOR("actor"),
    ACTORLIST("actorList"),
    TABULAR("tabular"),
    CASE_REF("caseRef"),
    DATETIME("dateTime"),
    BUTTON("button"),
    TASK_REF("taskRef"),
    FILTER("filter"),
    I18N("i18n"),
    STRING_COLLECTION("stringCollection");

    private final String name;

    FieldType(String name) {
        this.name = name;
    }

    public static FieldType fromString(String name) {
        return valueOf(name.toUpperCase());
    }

    public static FieldType fromName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Field type is null or blank");
        }
        return Arrays.stream(FieldType.values())
                .filter(v -> v.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(name + " is not a valid FieldType"));
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
