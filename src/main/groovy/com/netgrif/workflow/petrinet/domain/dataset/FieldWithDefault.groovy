package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
abstract class FieldWithDefault<T> extends Field<T> {

    protected T defaultValue

    FieldWithDefault() {
        super()
    }

    T getDefaultValue() {
        return defaultValue
    }

    void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue
    }
}
