package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.petrinet.domain.I18nString
import org.springframework.data.mongodb.core.mapping.Document

@Document
class EnumerationField extends ChoiceField<I18nString> {

    EnumerationField() {
        super()
    }

    EnumerationField(List<I18nString> values) {
        super(values as I18nString[])
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