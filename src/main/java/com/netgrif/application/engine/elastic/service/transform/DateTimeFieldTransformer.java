package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.elastic.domain.DateField;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.dataset.DateTimeField;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeFieldTransformer extends ElasticDataFieldTransformer<DateTimeField, DateField> {

    @Override
    public DateField transform(DateTimeField caseField, DateTimeField petriNetField) {
        LocalDateTime dateTime = caseField.getValue().getValue();
        if (dateTime == null) {
            return null;
        }
        String formattedValue = dateTime.format(DateTimeFormatter.BASIC_ISO_DATE);
        return new com.netgrif.application.engine.elastic.domain.DateField(formattedValue, dateTime);
    }

    @Override
    public DataType getType() {
        return DataType.DATE_TIME;
    }
}
