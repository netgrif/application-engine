package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.workflow.domain.dataset.BooleanField;
import org.springframework.stereotype.Component;

@Component
public class BooleanFieldTransformer extends ElasticDataFieldTransformer<BooleanField, com.netgrif.application.engine.elastic.domain.BooleanField> {

    @Override
    public com.netgrif.application.engine.elastic.domain.BooleanField transform(BooleanField caseField, BooleanField petriNetField) {
        Boolean value = caseField.getValue().getValue();
        if (value == null) {
            return null;
        }
        return new com.netgrif.application.engine.elastic.domain.BooleanField(value);
    }

    @Override
    public DataType getType() {
        return DataType.BOOLEAN;
    }
}
