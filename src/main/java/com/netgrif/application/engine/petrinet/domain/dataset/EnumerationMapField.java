package com.netgrif.application.engine.petrinet.domain.dataset;


import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.I18nString;

import java.util.Map;

public class EnumerationMapField extends MapOptionsField<I18nString, String> {

    public EnumerationMapField() {
        super();
    }

    public EnumerationMapField(Map<String, I18nString> choices) {
        super(choices);
    }

    public EnumerationMapField(Map<String, I18nString> choices, String defaultValue) {
        super(choices);
        this.defaultValue = defaultValue;
    }

    @Override
    public DataType getType() {
        return DataType.ENUMERATION_MAP;
    }

    public I18nString getSelectedOption() {
        if (this.getOptions() == null) {
            return null;
        }
        return this.getOptions().get(this.getValue());
    }

    @Override
    public EnumerationMapField clone() {
        EnumerationMapField clone = new EnumerationMapField();
        super.clone(clone);
        clone.options = options;
        clone.optionsExpression = optionsExpression;
        return clone;
    }
}
