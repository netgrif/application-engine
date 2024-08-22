package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.elastic.domain.MapField;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.application.engine.workflow.domain.DataFieldValue;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EnumerationMapFieldTransformer extends ElasticDataFieldTransformer<EnumerationMapField, MapField> {

    @Override
    public MapField transform(EnumerationMapField caseField, EnumerationMapField petriNetField) {
        Map<String, I18nString> options = caseField.getOptions() != null ? caseField.getOptions() : petriNetField.getOptions();
        DataFieldValue<String> selectedKey = caseField.getValue();
        String value = selectedKey != null ? selectedKey.getValue() : null;
        I18nString selectedValue = options.get(value) != null ? options.get(value) : new I18nString();
        return new MapField(value, selectedValue.collectTranslations(), caseField.getOptions());
    }

    @Override
    public DataType getType() {
        return DataType.ENUMERATION_MAP;
    }
}
