package com.netgrif.workflow.petrinet.domain.dataset;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document
public class MultichoiceField extends Field {

    private Set<String> choices;
    @Transient
    private Set<String> value;

    public MultichoiceField() {
        super();
        value = new HashSet<>();
        choices = new HashSet<>();
    }

    public MultichoiceField(String[] values) {
        this();
        if (values != null) {
            choices.addAll(Arrays.asList(values));
        }
    }

    public Set<String> getValue() {
        return value;
    }

    public Set<String> getChoices() {
        return choices;
    }

    @Override
    public void setType(FieldType type) {
        this.type = FieldType.MULTICHOICE;
    }

    @Override
    public void setValue(Object value) {
        if(value instanceof Set) this.value = (HashSet<String>) value;
        if(value instanceof List) this.value = new HashSet<>((List<String>)value);
    }
}