package com.netgrif.workflow.petrinet.domain.dataset

import lombok.Getter
import org.springframework.data.mongodb.core.mapping.Document

@Document //TODO 20.6.2017 - get to work
public class TabularField extends Field<ArrayList<ArrayList<Object>>> {
    @org.springframework.data.mongodb.core.mapping.Field
    @Getter
    private ArrayList<Field> fields;

    public TabularField() {
        this.fields = new ArrayList<>();
    }

    public void addField(Field field) {
        this.fields.add(field);
    }
}