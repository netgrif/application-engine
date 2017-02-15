package com.fmworkflow.petrinet.domain.dataset;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TextField extends Field {

    private String value;

    public TextField() {
        super();
    }

    public TextField(String value) {
        super();
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void modifyValue(String newValue) {
        this.value = newValue;
    }

    @Override
    public TextField copy(){
        TextField copied = new TextField(this.getValue());
        copied.set_id(this.get_id());
        copied.setDescription(this.getDescription());
        copied.setName(this.getName());

        return copied;
    }
}