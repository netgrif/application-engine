package com.netgrif.application.engine.workflow.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;

public class ButtonField extends Field<Integer> {

    public ButtonField() {
        super();
        this.setRawValue(0);
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.BUTTON;
    }

    @Override
    public ButtonField clone() {
        ButtonField clone = new ButtonField();
        super.clone(clone);
        return clone;
    }
}