package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;

@Data
//TODO release/8.0.0 @Document?
public class NumberField extends Field<Double> {

    public NumberField() {
        super();
    }

    @Override
    @QueryType(PropertyType.NONE)
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