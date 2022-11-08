package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.elastic.domain.TextField;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.I18nField;
import org.springframework.stereotype.Component;

@Component
public class I18nFieldTransformer extends ElasticDataFieldTransformer<I18nField, TextField> {

    @Override
    public TextField transform(I18nField caseField, I18nField petriNetField) {
        I18nString value = caseField.getValue().getValue();
        if (value == null) {
            return null;
        }
        return new TextField(value.collectTranslations());
    }

    @Override
    public DataType getType() {
        return DataType.I_18_N;
    }
}
