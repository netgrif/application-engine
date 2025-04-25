package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Setter
@Getter
public class DateField extends Field<LocalDate> {

    private String minDate;
    private String maxDate;

    public DateField() {
        super();
    }

    @Override
    public FieldType getType() {
        return FieldType.DATE;
    }

    public void setValue(Date value) {
        this.setValue(value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public void setDefaultValue(String value) {
        this.setDefaultValue(LocalDate.parse(value));
    }

    public void setDefaultValue(Date value) {
        this.setDefaultValue(value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    @Override
    public Field<?> clone() {
        DateField clone = new DateField();
        super.clone(clone);
        return clone;
    }
}
