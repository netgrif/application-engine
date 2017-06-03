package com.netgrif.workflow.petrinet.domain.dataset;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document
public class TabularField extends Field {
    @org.springframework.data.mongodb.core.mapping.Field
    private ArrayList<Field> fields;

    @Transient
    private ArrayList<ArrayList<Object>> values;

    public TabularField() {
        this.fields = new ArrayList<>();
    }

    @Override
    public void setType(FieldType type) {
        this.type = FieldType.TABULAR;
    }

    public ArrayList<ArrayList<Object>> getValues() {
        return values;
    }

    public ArrayList<Field> getFields() {
        return fields;
    }

    public void addField(Field field) {
        this.fields.add(field);
    }

    @Override
    public void setValue(Object value) {
        if (value != null)
            System.out.println(value.getClass());
    }
}