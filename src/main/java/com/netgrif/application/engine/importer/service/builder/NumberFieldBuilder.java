package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.NumberField;
import org.springframework.stereotype.Component;

@Component
public class NumberFieldBuilder extends FieldBuilder<NumberField> {

    @Override
    public NumberField build(Data data, Importer importer) {
        NumberField field = new NumberField();
        setDefaultValue(field, data, defaultValue -> {
            if (defaultValue != null) {
                field.setDefaultValue(Double.parseDouble(defaultValue));
            }
        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.NUMBER;
    }
}
