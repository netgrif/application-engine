package com.fmworkflow.petrinet.domain.dataset;


import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class UserField extends Field{

    @Transient
    private Long value;

    public UserField(){
        super();
    }

    public Long getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = (Long)value;
    }

    @Override
    public void setType(FieldType type) {
        this.type = FieldType.USER;
    }
}
