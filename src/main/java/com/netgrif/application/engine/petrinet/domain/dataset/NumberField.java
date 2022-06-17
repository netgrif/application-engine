package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import lombok.Data;

@Data
public class NumberField extends Field<Double> {

    public NumberField() {
        super();
        super.setDefaultValue(0.0d);
    }

    @Override
    public DataType getType() {
        return DataType.NUMBER;
    }

    @Override
    public NumberField clone() {
        NumberField clone = new NumberField();
        super.clone(clone);
        return clone;
    }
}