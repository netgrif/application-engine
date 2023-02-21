package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceMapField;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class MultichoiceMapFieldBuilder extends FieldBuilder<MultichoiceMapField> {

    @Override
    public MultichoiceMapField build(Data data, Importer importer) {
        MultichoiceMapField field = new MultichoiceMapField();
        initialize(field);
        setFieldOptions(field, data, importer);
        setDefaultValues(field, data, init -> {
            Set<String> defaultValue = new HashSet<>();
            if (init != null && !init.isEmpty()) {
                defaultValue.addAll(init);
            }
            field.setDefaultValue(defaultValue);
        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.MULTICHOICE_MAP;
    }
}
