package com.netgrif.application.engine.petrinet.domain.dataset;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import groovy.lang.GString;
import lombok.Data;

@Data
@JsonAutoDetect(
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
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