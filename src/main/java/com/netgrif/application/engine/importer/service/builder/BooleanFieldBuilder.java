package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.workflow.domain.dataset.BooleanField;
import org.springframework.stereotype.Component;

@Component
public class BooleanFieldBuilder extends FieldBuilder<BooleanField> {

    @Override
    public BooleanField build(Data data, Importer importer) {
        BooleanField field = new BooleanField();
        initialize(field);
        setDefaultValue(field, data, Boolean::parseBoolean);
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.BOOLEAN;
    }
}
