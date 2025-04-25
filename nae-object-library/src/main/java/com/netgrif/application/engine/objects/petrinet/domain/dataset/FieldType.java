package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import com.fasterxml.jackson.annotation.JsonValue;

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
    USER("user"),
    USERLIST("userList"),
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

    @JsonValue
    public String getName() {
        return name;
    }
}
