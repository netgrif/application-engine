package com.netgrif.workflow.petrinet.domain.dataset

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.mongodb.core.mapping.Document

@Document
abstract class FieldWithDefault<T> extends Field<T> {

    @JsonIgnore
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
