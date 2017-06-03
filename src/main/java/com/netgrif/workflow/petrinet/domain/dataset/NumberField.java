package com.netgrif.workflow.petrinet.domain.dataset;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class NumberField extends Field {

    @Transient
    private Double value;

    public NumberField() {
        super();
    }

    public Double getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = (Double) value;
    }

    @Override
    public void setType(FieldType type) {
        this.type = FieldType.NUMBER;
    }
}