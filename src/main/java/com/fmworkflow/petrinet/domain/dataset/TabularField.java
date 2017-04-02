package com.fmworkflow.petrinet.domain.dataset;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedList;
import java.util.List;

@Document
public class TabularField extends Field<List<Field>> {
    @Transient
    private List<Field> fields;

    public TabularField() {
        this.fields = new LinkedList<>();
    }

    @Override
    public void setType(FieldType type) {
        this.type = FieldType.TABULAR;
    }

    public List<Field> getValue() {
        return fields;
    }

    @Override
    public void setValue(List<Field> value) {
        super.setValue(value);
    }
}