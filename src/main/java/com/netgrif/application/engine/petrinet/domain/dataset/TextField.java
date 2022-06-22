package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;

@Data
public class TextField extends Field<String> {

    public TextField() {
        super();
    }

    @Override
    @QueryType(PropertyType.NONE)
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