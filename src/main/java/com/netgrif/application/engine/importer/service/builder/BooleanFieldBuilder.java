package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.BooleanField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
public class BooleanFieldBuilder extends FieldBuilder<BooleanField> {

    @Override
    public BooleanField build(Data data, Importer importer) {
        BooleanField field = new BooleanField();
        setDefaultValue(field, data, defaultValue -> {
            if (defaultValue != null) {
                field.setDefaultValue(Boolean.valueOf(defaultValue));
            }
        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.BOOLEAN;
    }
}
