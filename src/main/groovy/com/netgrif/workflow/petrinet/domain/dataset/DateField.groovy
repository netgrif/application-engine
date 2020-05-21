package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document

import java.time.LocalDate
import java.time.ZoneId

@Document
class DateField extends ValidableField<LocalDate> {

    @Transient
    private String minDate

    @Transient
    private String maxDate

    DateField() {
        super()
    }

    @Override
    FieldType getType() {
        return FieldType.DATE
    }

    @Override
    void clearValue() {
        super.clearValue()
        setValue(getDefaultValue())
    }

    void setValue(Date value) {
        this.value = value?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
    }

    void setDefaultValue(String value) {
        this.setDefaultValue(LocalDate.parse(value))
    }

    void setDefaultValue(Date value) {
        this.setDefaultValue(value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
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
        DateField clone = new DateField()
        super.clone(clone)
        clone.validations = this.validations
        clone.defaultValue = this.defaultValue
        return clone
    }
}
