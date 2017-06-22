package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

import java.time.LocalDate
import java.time.ZoneId

@Document
public class DateField extends Field<LocalDate> {

    public DateField() {
        super();
    }

    public void setValue(Date value) {
        this.value = ((Date)value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
