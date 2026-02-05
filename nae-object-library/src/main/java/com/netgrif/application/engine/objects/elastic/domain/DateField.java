package com.netgrif.application.engine.objects.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class DateField extends DataField {

    public LocalDateTime dateValue;

    public Long timestampValue;

    public DateField(DateField field) {
        super(field);
        this.dateValue = field.dateValue;
        this.timestampValue = field.timestampValue;
    }

    public DateField(String value, LocalDateTime dateTime) {
        super(value);
        this.dateValue = dateTime;
        this.timestampValue = Timestamp.valueOf(dateTime).getTime();
    }

    @Override
    public Object getValue() {
        return dateValue;
    }
}
