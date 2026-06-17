package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;

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
        this.setDefaultValue(defaultValue);
    }

    @Override
    public FieldType getType() {
        return FieldType.ENUMERATION_MAP;
    }

    @Override
    public void setOptions(Map<String, I18nString> options) {
        super.setOptions(options);
    }

    @Override
    public void setDefaultValue(String defaultValue) {
        super.setDefaultValue(defaultValue);
    }

    @Override
    public Field<?> clone() {
        EnumerationMapField clone = new EnumerationMapField();
        super.clone(clone);
        clone.setOptions(getOptions());
        clone.optionsExpression = optionsExpression;
        return clone;
    }

}
