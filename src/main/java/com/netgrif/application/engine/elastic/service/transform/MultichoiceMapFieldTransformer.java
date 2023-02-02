package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.elastic.domain.MapField;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceMapField;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MultichoiceMapFieldTransformer extends ElasticDataFieldTransformer<MultichoiceMapField, MapField> {

    @Override
    public MapField transform(MultichoiceMapField caseField, MultichoiceMapField petriNetField) {
        Set<String> values = caseField.getRawValue();
        if (values == null || values.isEmpty()) {
            return null;
        }
        Map<String, I18nString> options = caseField.getOptions() != null ? caseField.getOptions() : petriNetField.getOptions();
        Map<String, List<String>> fieldValues = new HashMap<>();
        for (String key : values) {
            fieldValues.put(key, options.get(key).collectTranslations());
        }
        return new MapField(fieldValues);
    }

    @Override
    public DataType getType() {
        return DataType.MULTICHOICE_MAP;
    }
}
