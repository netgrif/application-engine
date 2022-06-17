package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;

public class BooleanField extends Field<Boolean> {

    public BooleanField() {
        super();
        defaultValue = false;
    }

    @Override
    public DataType getType() {
        return DataType.BOOLEAN;
    }

    public Boolean or(final BooleanField field) {
        return this.getValue() || field.getValue();
    }

    public Boolean and(final BooleanField field) {
        return this.getValue() && field.getValue();
    }

    @Override
    public BooleanField clone() {
        BooleanField clone = new BooleanField();
        super.clone(clone);
        return clone;
    }
}