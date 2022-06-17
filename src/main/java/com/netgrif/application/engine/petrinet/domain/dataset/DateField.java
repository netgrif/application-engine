package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;

import java.time.LocalDate;

public class DateField extends Field<LocalDate> {

    public DateField() {
        super();
    }

    @Override
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
