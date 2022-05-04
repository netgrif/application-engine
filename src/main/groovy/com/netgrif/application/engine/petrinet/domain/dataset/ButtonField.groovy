package com.netgrif.application.engine.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class ButtonField extends Field<Integer> {

    ButtonField() {
        super()
        value = 0
    }

    @Override
    FieldType getType() {
        return FieldType.BUTTON
    }

    @Override
    Field clone() {
        ButtonField clone = new ButtonField()
        super.clone(clone)
        return clone
    }
}