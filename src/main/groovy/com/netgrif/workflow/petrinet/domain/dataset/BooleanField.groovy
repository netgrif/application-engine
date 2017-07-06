package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
public class BooleanField extends FieldWithDefault<Boolean> {

    public BooleanField() {
        super();
        setDefaultValue(false)
    }

    void setDefaultValue(String defaultValue) {
        super.setDefaultValue(Boolean.parseBoolean(defaultValue))
    }

    Boolean or(final BooleanField field){
        return this.value || field.value
    }

    Boolean and(final BooleanField field){
        return this.value && field.value
    }
}
