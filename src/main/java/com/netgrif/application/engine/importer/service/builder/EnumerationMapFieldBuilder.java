package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField;
import org.springframework.stereotype.Component;

@Component
public class EnumerationMapFieldBuilder extends FieldBuilder<EnumerationMapField> {

    @Override
    public EnumerationMapField build(Data data, Importer importer) {
        EnumerationMapField field = new EnumerationMapField();
        initialize(field);
        setFieldOptions(field, data, importer);
        setDefaultValue(field, data, init -> {
            if (init != null && !init.isEmpty()) {
                field.setDefaultValue(init);
            }
        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.ENUMERATION_MAP;
    }
}
