package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.elastic.domain.TextField;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceField;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MultichoiceFieldTransformer extends ElasticDataFieldTransformer<MultichoiceField, TextField> {

    @Override
    public TextField transform(MultichoiceField caseField, MultichoiceField petriNetField) {
        Set<I18nString> values = caseField.getValue().getValue();
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<String> translations = values.stream()
                .map(I18nString::collectTranslations)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return new TextField(translations);
    }

    @Override
    public DataType getType() {
        return DataType.MULTICHOICE;
    }
}
