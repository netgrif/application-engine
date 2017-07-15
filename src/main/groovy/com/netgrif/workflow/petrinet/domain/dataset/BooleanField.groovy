package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
public class BooleanField extends FieldWithDefault<Boolean> {

    public BooleanField() {
        super();
        super.defaultValue = false
    }

    @Override
    void setDefaultValue(String defaultValue) {
        super.defaultValue = Boolean.parseBoolean(defaultValue)
    }

    @Override
    void clearValue() {
        super.clearValue()
        setValue(getDefaultValue())
    }

    Boolean or(final BooleanField field){
        return this.value || field.value
    }

    Boolean and(final BooleanField field){
        return this.value && field.value
    }
}
