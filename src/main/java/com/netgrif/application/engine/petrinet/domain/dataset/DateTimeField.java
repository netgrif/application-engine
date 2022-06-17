package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;

import java.time.LocalDateTime;

public class DateTimeField extends Field<LocalDateTime> {

    public DateTimeField() {
        super();
    }

    @Override
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