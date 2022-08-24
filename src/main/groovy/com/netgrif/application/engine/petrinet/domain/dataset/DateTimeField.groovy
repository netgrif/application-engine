package com.netgrif.application.engine.petrinet.domain.dataset

import org.springframework.data.annotation.Transient

import java.time.LocalDateTime
import java.time.ZoneId;

class DateTimeField extends Field<LocalDateTime> {

    @Transient
    private String minDate

    @Transient
    private String maxDate


    private TimeZone timeZone

    DateTimeField() {
        super()
        this.timeZone = new TimeZone(ZoneId.systemDefault())
    }

    @Override
    FieldType getType() {
        return FieldType.DATETIME
    }


    void setValue(Date value) {
        this.value = value?.toInstant()?.atZone(this.timeZone.zoneId)?.toLocalDateTime()
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

    TimeZone getTimeZone() {
        return timeZone
    }

    void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone
    }

    @Override
    Field clone() {
        DateTimeField clone = new DateTimeField()
        super.clone(clone)
        return clone
    }
}