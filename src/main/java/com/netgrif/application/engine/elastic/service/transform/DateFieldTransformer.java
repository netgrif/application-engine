package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.dataset.DateField;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateFieldTransformer extends ElasticDataFieldTransformer<DateField, com.netgrif.application.engine.elastic.domain.DateField> {

    @Override
    public com.netgrif.application.engine.elastic.domain.DateField transform(DateField caseField, DateField petriNetField) {
        LocalDate date = caseField.getValue().getValue();
        if (date == null) {
            return null;
        }
        String formattedValue = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.NOON);
        return new com.netgrif.application.engine.elastic.domain.DateField(formattedValue, dateTime);
    }

    @Override
    public DataType getType() {
        return DataType.DATE;
    }
}
