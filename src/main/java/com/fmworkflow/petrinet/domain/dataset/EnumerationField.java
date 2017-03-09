package com.fmworkflow.petrinet.domain.dataset;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Document
public class EnumerationField extends Field {

    private Set<String> choices;
    @Transient
    private String value;

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

    public String getValue() {
        return value;
    }

    public Set<String> getChoices() {
        return choices;
    }

    @Override
    public void setType(FieldType type) {
        this.type = FieldType.FILE;
    }

    @Override
    public void setValue(Object value) {
        this.value = (String) value;
    }
}