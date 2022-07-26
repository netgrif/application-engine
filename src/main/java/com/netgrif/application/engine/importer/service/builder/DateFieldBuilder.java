package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.DateField;
import org.springframework.stereotype.Component;

import static com.netgrif.application.engine.importer.service.FieldFactory.parseDate;

@Component
public class DateFieldBuilder extends FieldBuilder<DateField> {

    @Override
    public DateField build(Data data, Importer importer) {
        DateField field = new DateField();
        setDefaultValue(field, data, defaultValue -> {
            if (defaultValue != null) {
                field.setDefaultValue(parseDate(defaultValue));
            }
        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.DATE;
    }
}
