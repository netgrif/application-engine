package com.fmworkflow.petrinet.domain.dataset;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.File;

@Document
public class FileField extends Field {

    @Transient
    private String value;

    public FileField() {
        super();
    }

    public String getValue() {
        return value;
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