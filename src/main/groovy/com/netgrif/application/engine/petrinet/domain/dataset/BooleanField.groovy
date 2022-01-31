package com.netgrif.application.engine.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class BooleanField extends Field<Boolean> {

    BooleanField() {
        super()
        defaultValue = false
    }

    @Override
    FieldType getType() {
        return FieldType.BOOLEAN
    }

    Boolean or(final BooleanField field) {
        return this.value || field.value
    }

    Boolean and(final BooleanField field) {
        return this.value && field.value
    }

    @Override
    Field clone() {
        BooleanField clone = new BooleanField()
        super.clone(clone)
        return clone
    }
}