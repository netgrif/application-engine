package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;

public class BooleanField extends Field<Boolean> {

    public BooleanField() {
        super();
        defaultValue = false;
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.BOOLEAN;
    }

    @Override
    public BooleanField clone() {
        BooleanField clone = new BooleanField();
        super.clone(clone);
        return clone;
    }
}