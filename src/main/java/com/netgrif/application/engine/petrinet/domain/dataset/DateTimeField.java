package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;

import java.time.LocalDateTime;

public class DateTimeField extends Field<LocalDateTime> {

    public DateTimeField() {
        super();
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.DATE_TIME;
    }

    @Override
    public DateTimeField clone() {
        DateTimeField clone = new DateTimeField();
        super.clone(clone);
        return clone;
    }
}