package com.netgrif.workflow.petrinet.domain.dataset

import java.time.LocalDateTime
import java.time.ZoneId;

class DateTimeField extends ValidableField<LocalDateTime> {

    DateTimeField() {
        super()
    }

    @Override
    FieldType getType() {
        return FieldType.DATETIME
    }

    @Override
    void clearValue() {
        super.clearValue()
        setValue(getDefaultValue())
    }

    void setValue(Date value) {
        this.value = value?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
    }

    @Override
    Field clone() {
        DateTimeField clone = new DateTimeField()
        super.clone(clone)

        clone.validationRules = this.validationRules
        clone.defaultValue = this.defaultValue

        return clone
    }
}