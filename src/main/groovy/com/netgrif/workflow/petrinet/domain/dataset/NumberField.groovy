package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document

@Document
public class NumberField extends Field<Double> {

    @Transient
    private Double minValue
    @Transient
    private Double maxValue

    public NumberField() {
        super();
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