package com.netgrif.application.engine.adapter.spring.elastic.domain;

import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

import static org.springframework.data.elasticsearch.annotations.FieldType.Long;
import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@NoArgsConstructor
public class DateField extends com.netgrif.application.engine.objects.elastic.domain.DateField {

    public DateField(DateField field) {
        super(field);
    }

    public DateField(String value, LocalDateTime dateTime) {
        super(value, dateTime);
    }

    @Override
    @Field(type = Text)
    public String[] getFulltextValue() {
        return super.getFulltextValue();
    }

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    public LocalDateTime getDateValue() {
        return dateValue;
    }

    @Field(type = Long)
    public Long getTimestampValue() {
        return timestampValue;
    }
}
