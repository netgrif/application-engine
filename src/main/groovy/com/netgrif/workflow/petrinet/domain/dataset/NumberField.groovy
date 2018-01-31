package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document

@Document
class NumberField extends ValidableField<Double> {

    @Transient
    private Double minValue

    @Transient
    private Double maxValue

    NumberField() {
        super()
        super.superSetDefaultValue(0)
    }

    @Override
    FieldType getType() {
        return FieldType.NUMBER
    }

    @Override
    void setDefaultValue(String value) {
        super.superSetDefaultValue(Double.parseDouble(value))
    }

    @Override
    void clearValue() {
        super.clearValue()
        setValue(getDefaultValue())
    }

    Double getMinValue() {
        return minValue
    }

    void setMinValue(Double minValue) {
        this.minValue = minValue
    }

    Double getMaxValue() {
        return maxValue
    }

    void setMaxValue(Double maxValue) {
        this.maxValue = maxValue
    }
}