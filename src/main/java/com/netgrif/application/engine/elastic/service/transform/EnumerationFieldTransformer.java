package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.elastic.domain.TextField;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationField;
import org.springframework.stereotype.Component;

@Component
public class EnumerationFieldTransformer extends ElasticDataFieldTransformer<EnumerationField, TextField> {

    @Override
    public TextField transform(EnumerationField caseField, EnumerationField petriNetField) {
        I18nString value = caseField.getValue().getValue();
        if (value == null) {
            return null;
        }
        return new TextField(value.collectTranslations());
    }

    @Override
    public DataType getType() {
        return DataType.ENUMERATION;
    }
}
