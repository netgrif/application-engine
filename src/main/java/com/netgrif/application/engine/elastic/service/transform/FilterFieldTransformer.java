package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.elastic.domain.TextField;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.workflow.domain.dataset.FilterField;
import org.springframework.stereotype.Component;

@Component
public class FilterFieldTransformer extends ElasticDataFieldTransformer<FilterField, TextField> {

    @Override
    public TextField transform(FilterField caseField, FilterField petriNetField) {
        String value = caseField.getValue().getValue();
        if (value == null || value.isBlank()) {
            return null;
        }
        return new TextField(value);
    }

    @Override
    public DataType getType() {
        return DataType.FILTER;
    }
}
