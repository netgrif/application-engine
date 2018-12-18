package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class ButtonField extends Field<String> {

    ButtonField() {
        super()
        value = ""
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