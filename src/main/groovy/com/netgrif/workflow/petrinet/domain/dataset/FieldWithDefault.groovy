package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
abstract class FieldWithDefault<T> extends Field<T> {

    protected T defaultValue
    protected String initExpression

    FieldWithDefault() {
        super()
    }

    T getDefaultValue() {
        return defaultValue
    }

    String getExpression() {
        return initExpression
    }

    void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue
    }

    void setDynamicExpression(String expression) {
        this.initExpression = expression
    }

    boolean isDynamicDefaultValue() {
        return initExpression != null
    }

}