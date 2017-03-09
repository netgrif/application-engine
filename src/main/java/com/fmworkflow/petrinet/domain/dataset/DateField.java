package com.fmworkflow.petrinet.domain.dataset;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document
public class DateField extends Field {

    @Transient
    private LocalDate value;

    public DateField() {
        super();
    }

    public LocalDate getValue() {
        return value;
    }

    @Override
    public void setType(FieldType type) {
        this.type = FieldType.DATE;
    }

    @Override
    public void setValue(Object value) {
        this.value = (LocalDate) value;
    }
}
