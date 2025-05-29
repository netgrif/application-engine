package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.DateField;
import com.netgrif.application.engine.utils.DateUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Optional;

@Component
public class DateFieldBuilder extends FieldBuilder<DateField> {

    @Override
    public DateField build(Data data, Importer importer) {
        DateField field = new DateField();
        initialize(field);
        // TODO: release/8.0.0
        setDefaultValue(field, data,s -> LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE));
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.DATE;
    }
}
