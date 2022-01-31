package com.netgrif.application.engine.petrinet.domain.dataset

import org.springframework.data.annotation.Transient

import java.time.LocalDateTime
import java.time.ZoneId;

class DateTimeField extends Field<LocalDateTime> {

    @Transient
    private String minDate

    @Transient
    private String maxDate

    DateTimeField() {
        super()
    }

    @Override
    FieldType getType() {
        return FieldType.DATETIME
    }


    void setValue(Date value) {
        this.value = value?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
    }

    String getMinDate() {
        return minDate
    }

    void setMinDate(String minDate) {
        this.minDate = minDate
    }

    String getMaxDate() {
        return maxDate
    }

    void setMaxDate(String maxDate) {
        this.maxDate = maxDate
    }

    @Override
    Field clone() {
        DateTimeField clone = new DateTimeField()
        super.clone(clone)
        return clone
    }
}