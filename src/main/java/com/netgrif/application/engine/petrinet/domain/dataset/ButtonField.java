package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;

public class ButtonField extends Field<String> {

    public ButtonField() {
        super();
        this.setValue("");
    }

    @Override
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