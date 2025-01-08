package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.workflow.domain.dataset.DateTimeField;
import com.netgrif.application.engine.utils.DateUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;


@Component
public class DateTimeFieldBuilder extends FieldBuilder<DateTimeField> {

    @Override
    public DateTimeField build(Data data, Importer importer) {
        DateTimeField field = new DateTimeField();
        initialize(field);
        setDefaultValue(field, data, s -> {
            Optional<LocalDateTime> localDateTime = DateUtils.parseDateTime(s);
            return localDateTime.orElse(null);
        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.DATE_TIME;
    }
}
