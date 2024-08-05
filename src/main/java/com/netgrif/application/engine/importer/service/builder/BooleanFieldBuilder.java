package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.BooleanField;
import com.netgrif.application.engine.workflow.domain.DataFieldBehaviors;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class BooleanFieldBuilder extends FieldBuilder<BooleanField> {

    @Override
    public BooleanField build(Data data, Importer importer) {
        BooleanField field = new BooleanField();
        initialize(field);
        // TODO: release/8.0.0
//        setDefaultValue(field, data, defaultValue -> {
//            if (defaultValue != null) {
//                field.setDefaultValue(Boolean.valueOf(defaultValue));
//            }
//        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.BOOLEAN;
    }
}
