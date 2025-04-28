package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;

import java.util.LinkedHashSet;
import java.util.Map;

public class MultichoiceMapField extends MapOptionsField<I18nString, LinkedHashSet<String>> {
    public MultichoiceMapField() {
        super();
        this.setDefaultValue(new LinkedHashSet<String>());
    }

    public MultichoiceMapField(Map<String, I18nString> choices) {
        super(choices);
        this.setDefaultValue(new LinkedHashSet<String>());
    }

    public MultichoiceMapField(Map<String, I18nString> choices, LinkedHashSet<String> defaultValues) {
        this(choices);
        this.setDefaultValue(defaultValues);
    }

    @Override
    public FieldType getType() {
        return FieldType.MULTICHOICE_MAP;
    }

    @Override
    public void setOptions(Map<String, I18nString> options) {
        super.setOptions(options);
    }

    @Override
    public void setDefaultValue(LinkedHashSet<String> defaultValue) {
        super.setDefaultValue(defaultValue);
    }

    @Override
    public Field<?> clone() {
        MultichoiceMapField clone = new MultichoiceMapField();
        super.clone(clone);
        clone.setOptions(getOptions());
        clone.optionsExpression = optionsExpression;
        return clone;
    }
}
