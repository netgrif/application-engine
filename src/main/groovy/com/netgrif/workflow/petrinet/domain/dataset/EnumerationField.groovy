package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
public class EnumerationField extends ChoiceField<String> {

    public EnumerationField() {
        super();
    }

    public EnumerationField(String[] values) {
        super(values)
    }

    @Override
    void clearValue() {
        super.clearValue()
        setValue(getDefaultValue())
    }
}