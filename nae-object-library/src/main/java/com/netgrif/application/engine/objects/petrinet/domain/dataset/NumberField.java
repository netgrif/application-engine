package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NumberField extends Field<Double> {

    private Double minValue;
    private Double maxValue;

    public NumberField() {
        super();
        super.setDefaultValue(0.0d);
    }

    @Override
    public FieldType getType() {
        return FieldType.NUMBER;
    }

    @Override
    public Field<?> clone() {
        NumberField clone = new NumberField();
        super.clone(clone);

        return clone;
    }
}
