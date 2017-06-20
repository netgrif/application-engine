package com.netgrif.workflow.petrinet.domain.dataset;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

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
        if(value instanceof  LocalDate) this.value = (LocalDate) value;
        else if(value instanceof Date) this.value = ((Date)value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
