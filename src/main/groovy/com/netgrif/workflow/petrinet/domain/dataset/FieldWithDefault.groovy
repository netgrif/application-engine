package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
abstract class FieldWithDefault<T> extends Field<T> {

    private T defaultValue

    FieldWithDefault() {
        super()
    }

    T getDefaultValue() {
        return defaultValue
    }

    void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue
    }
}
