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
        // TODO: release/8.0.0 ArrayList cannot be cast to Set
        Set<String> values = caseField.getRawValue();
        if (values == null || values.isEmpty()) {
            return null;
        }
        Map<String, I18nString> options = caseField.getOptions() != null ? caseField.getOptions() : petriNetField.getOptions();
        Map<String, List<String>> fieldValues = new HashMap<>();
        for (String value : values) {
            // TODO: release/8.0.0 refactor, duplicate with EnumMap transformer, probably unwanted values could be saved?
            I18nString selectedValue = options.get(value) != null ? options.get(value) : new I18nString();
            fieldValues.put(value, selectedValue.collectTranslations());
        }
        return new MapField(fieldValues);
    }

    @Override
    public DataType getType() {
        return DataType.MULTICHOICE_MAP;
    }
}
