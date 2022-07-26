package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceMapField;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class MultichoiceMapFieldBuilder extends FieldBuilder<MultichoiceMapField> {

    @Override
    public MultichoiceMapField build(Data data, Importer importer) {
        MultichoiceMapField field = new MultichoiceMapField();
        setFieldOptions(field, data, importer);
        setDefaultValues(field, data, init -> {
            if (init != null && !init.isEmpty()) {
                field.setDefaultValue(new HashSet<>(init));
            }
        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.MULTICHOICE_MAP;
    }
}
