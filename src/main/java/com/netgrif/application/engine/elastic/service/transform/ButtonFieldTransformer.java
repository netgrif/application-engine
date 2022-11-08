package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.dataset.ButtonField;
import org.springframework.stereotype.Component;

@Component
public class ButtonFieldTransformer extends ElasticDataFieldTransformer<ButtonField, com.netgrif.application.engine.elastic.domain.ButtonField> {

    @Override
    public com.netgrif.application.engine.elastic.domain.ButtonField transform(ButtonField caseField, ButtonField petriNetField) {
        Integer value = caseField.getValue().getValue();
        if (value == null) {
            return null;
        }
        return new com.netgrif.application.engine.elastic.domain.ButtonField(value);
    }

    @Override
    public DataType getType() {
        return DataType.BUTTON;
    }
}
