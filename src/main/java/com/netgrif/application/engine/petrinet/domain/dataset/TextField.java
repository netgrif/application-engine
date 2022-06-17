package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import lombok.Data;

@Data
public class TextField extends Field<String> {

    public TextField() {
        super();
    }

    @Override
    public DataType getType() {
        return DataType.TEXT;
    }

    @Override
    public TextField clone() {
        TextField clone = new TextField();
        super.clone(clone);
        return clone;
    }
}