package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import org.springframework.stereotype.Component;

@Component
public class TextFieldTransformer extends ElasticDataFieldTransformer<TextField, com.netgrif.application.engine.elastic.domain.TextField> {

    @Override
    public com.netgrif.application.engine.elastic.domain.TextField transform(TextField caseField, TextField petriNetField) {
        String value = caseField.getValue().getValue();
        if (value == null || value.isBlank()) {
            return null;
        }
        return new com.netgrif.application.engine.elastic.domain.TextField(value);
    }

    @Override
    public DataType getType() {
        return DataType.TEXT;
    }
}
