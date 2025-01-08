package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.workflow.domain.dataset.StringCollectionField;
import org.springframework.stereotype.Component;

@Component
public class StringCollectionFieldBuilder extends FieldBuilder<StringCollectionField> {

    @Override
    public StringCollectionField build(Data data, Importer importer) {
        StringCollectionField field = new StringCollectionField();
        initialize(field);
        // TODO: release/8.0.0
        setDefaultValue(field, data);
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.STRING_COLLECTION;
    }
}
