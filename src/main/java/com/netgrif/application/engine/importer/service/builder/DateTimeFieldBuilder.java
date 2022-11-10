package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.DateTimeField;
import org.springframework.stereotype.Component;


@Component
public class DateTimeFieldBuilder extends FieldBuilder<DateTimeField> {

    @Override
    public DateTimeField build(Data data, Importer importer) {
        DateTimeField field = new DateTimeField();
//        TODO: NAE-1645 default format?
//        setDefaultValue(field, data, defaultValue -> field.setDefaultValue(parseDateTime(defaultValue)));
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.DATE_TIME;
    }
}
