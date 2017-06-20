package com.netgrif.workflow.petrinet.domain.dataset;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class BooleanField extends Field {

    @Transient
    private Boolean value;

    public BooleanField() {
        super();
    }

    public Boolean getValue() {
        return value;
    }

    @Override
    public void setType(FieldType type) {
        this.type = FieldType.BOOLEAN;
    }

    @Override
    public void setValue(Object value) {
        this.value = (Boolean) value;
    }
}
