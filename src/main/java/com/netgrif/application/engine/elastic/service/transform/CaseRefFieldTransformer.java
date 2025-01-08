package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.elastic.domain.TextField;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.workflow.domain.dataset.CaseField;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CaseRefFieldTransformer extends ElasticDataFieldTransformer<CaseField, TextField> {

    @Override
    public TextField transform(CaseField caseField, CaseField petriNetField) {
        List<String> value = caseField.getValue().getValue();
        if (value == null || value.isEmpty()) {
            return null;
        }
        return new TextField(value);
    }

    @Override
    public DataType getType() {
        return DataType.CASE_REF;
    }
}
