package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class NumberField extends DataField {

    protected Double numberValue;

    public NumberField(NumberField field) {
        super(field);
        this.numberValue = field.numberValue;
    }

    public NumberField(Double value) {
        super(value.toString());
        this.numberValue = value;
    }

    public Object getValue() {
        return this.numberValue;
    }
}
