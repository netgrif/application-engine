package com.netgrif.application.engine.petrinet.domain.dataset

import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document

@Document
class NumberField extends Field<Double> {

    @Transient
    private Double minValue

    @Transient
    private Double maxValue

    NumberField() {
        super()
        super.setDefaultValue(0.0d)
    }

    @Override
    FieldType getType() {
        return FieldType.NUMBER
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

    @Override
    Field clone() {
        NumberField clone = new NumberField()
        super.clone(clone)

        return clone
    }
}
