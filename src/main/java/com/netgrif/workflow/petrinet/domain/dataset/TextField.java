package com.netgrif.workflow.petrinet.domain.dataset;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TextField extends Field<String> {

    @Transient
    private String value;

    public TextField() {
        super();
    }

    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void setType(FieldType type){
        this.type = FieldType.TEXT;
    }
}