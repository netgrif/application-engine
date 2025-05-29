package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceMapField;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class MultichoiceMapFieldBuilder extends MapOptionsFieldBuilder<MultichoiceMapField, Set<String>> {

    @Override
    public MultichoiceMapField build(Data data, Importer importer) {
        MultichoiceMapField field = new MultichoiceMapField();
        initialize(field);
        setFieldOptions(field, data, importer);
        setDefaultValue(field, data, Set::of);
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.MULTICHOICE_MAP;
    }
}
