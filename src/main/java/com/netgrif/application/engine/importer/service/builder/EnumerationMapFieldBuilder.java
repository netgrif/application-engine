package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.workflow.domain.dataset.EnumerationMapField;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class EnumerationMapFieldBuilder extends MapOptionsFieldBuilder<EnumerationMapField, String> {

    @Override
    public EnumerationMapField build(Data data, Importer importer) {
        EnumerationMapField field = new EnumerationMapField();
        initialize(field);
        setFieldOptions(field, data, importer);
        setDefaultValue(field, data, Function.identity());
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.ENUMERATION_MAP;
    }
}
