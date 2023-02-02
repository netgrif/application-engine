package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.DateField;
import com.netgrif.application.engine.utils.DateUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class DateFieldBuilder extends FieldBuilder<DateField> {

    @Override
    public DateField build(Data data, Importer importer) {
        DateField field = new DateField();
        initialize(field);
        setDefaultValue(field, data, defaultValueString -> {
            if (defaultValueString == null) {
                return;
            }
            Optional<LocalDate> defaultValue = DateUtils.parseDate(defaultValueString);
            field.setDefaultValue(defaultValue.orElse(null));
        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.DATE;
    }
}
