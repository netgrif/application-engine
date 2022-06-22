package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;

import java.time.LocalDate;

public class DateField extends Field<LocalDate> {

    public DateField() {
        super();
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.DATE;
    }

    @Override
    public DateField clone() {
        DateField clone = new DateField();
        super.clone(clone);
        return clone;
    }
}
