package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
public class BooleanField extends Field<Boolean> {

    public BooleanField() {
        super();
    }

    Boolean or(final BooleanField field){
        return this.value || field.value
    }

    Boolean and(final BooleanField field){
        return this.value && field.value
    }
}
