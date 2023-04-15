package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.model.I18NStringTypeWithExpression;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TextFieldBuilder extends FieldBuilder<TextField> {

    @Override
    public TextField build(Data data, Importer importer) {
        TextField field = new TextField();
        initialize(field);
        String value = null;
        List<I18NStringTypeWithExpression> values = data.getValues();
        if (values != null && !values.isEmpty()) {
            value = values.get(0).getValue();
        }
        field.setRawValue(value); // TODO: release/7.0.0 is it necessary?
        setDefaultValue(field, data, field::setDefaultValue);
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.TEXT;
    }
}
