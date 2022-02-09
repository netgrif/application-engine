package com.netgrif.application.engine.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.springframework.data.elasticsearch.annotations.FieldType.Long;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DateField extends DataField {

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime dateValue;

    @Field(type = Long)
    private Long timestampValue;

    public DateField(String value, LocalDateTime dateTime) {
        super(value);
        this.dateValue = dateTime;
        this.timestampValue = Timestamp.valueOf(dateTime).getTime();
    }
}