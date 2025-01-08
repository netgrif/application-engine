package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.workflow.domain.dataset.TextField;
import org.springframework.stereotype.Component;

@Component
public class TextFieldBuilder extends FieldBuilder<TextField> {

    @Override
    public TextField build(Data data, Importer importer) {
        TextField field = new TextField();
        initialize(field);
        setDefaultValue(field, data, s -> s);
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.TEXT;
    }
}
