package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class EnumerationField extends ChoiceField<String> {

    EnumerationField() {
        super()
    }

    EnumerationField(List<String> values) {
        super(values)
    }

    @Override
    FieldType getType() {
        return FieldType.ENUMERATION
    }

    @Override
    void clearValue() {
        super.clearValue()
        setValue(getDefaultValue())
    }
}