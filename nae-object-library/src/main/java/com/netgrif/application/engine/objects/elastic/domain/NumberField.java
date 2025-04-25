package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class NumberField extends DataField {

    public Double numberValue;

    public NumberField(Double value) {
        super(value.toString());
        this.numberValue = value;
    }
}
