package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
public class EnumerationField extends FieldWithDefault<String> {

    private Set<String> choices;

    public EnumerationField() {
        super();
        choices = new HashSet<>();
    }

    public EnumerationField(String[] values) {
        this();
        if (values != null) {
            choices.addAll(Arrays.asList(values));
        }
    }

    @Override
    void clearValue() {
        super.clearValue()
        setValue(getDefaultValue())
    }

    Set<String> getChoices() {
        return choices
    }
}