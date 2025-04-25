package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Setter
@Getter
public class DateTimeField extends Field<LocalDateTime> {
    private String minDate;
    private String maxDate;

    public DateTimeField() {
        super();
    }

    @Override
    public FieldType getType() {
        return FieldType.DATETIME;
    }

    public void setValue(Date value) {
        this.setValue(value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    @Override
    public Field<?> clone() {
        DateTimeField clone = new DateTimeField();
        super.clone(clone);
        return clone;
    }
}
