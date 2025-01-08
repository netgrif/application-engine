package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.workflow.domain.dataset.NumberField;
import org.springframework.stereotype.Component;

@Component
public class NumberFieldTransformer extends ElasticDataFieldTransformer<NumberField, com.netgrif.application.engine.elastic.domain.NumberField> {

    @Override
    public com.netgrif.application.engine.elastic.domain.NumberField transform(NumberField caseField, NumberField petriNetField) {
        Double value = caseField.getValue().getValue();
        if (value == null) {
            return null;
        }
        return new com.netgrif.application.engine.elastic.domain.NumberField(value);
    }

    @Override
    public DataType getType() {
        return DataType.NUMBER;
    }
}
