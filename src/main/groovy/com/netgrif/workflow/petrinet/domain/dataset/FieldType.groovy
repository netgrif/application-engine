package com.netgrif.workflow.petrinet.domain.dataset

import com.fasterxml.jackson.annotation.JsonValue

enum FieldType {

    TEXT("text"),
    DATE("date"),
    BOOLEAN("boolean"),
    FILE("file"),
    FILELIST('fileList'),
    ENUMERATION("enumeration"),
    ENUMERATION_MAP("enumeration_map"),
    MULTICHOICE("multichoice"),
    MULTICHOICE_MAP("multichoice_map"),
    NUMBER("number"),
    USER("user"),
    TABULAR("tabular"),
    CASE_REF("caseRef"),
    DATETIME("dateTime"),
    BUTTON("button"),
    TASK_REF("taskRef")

    String name

    FieldType(String name) {
        this.name = name
    }

    static FieldType fromString(String name) {
        return valueOf(name.toUpperCase())
    }

    @JsonValue
    String getName() {
        return name
    }
}
