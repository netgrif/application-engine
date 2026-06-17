package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;

import java.util.*;

public class MultichoiceField extends ChoiceField<LinkedHashSet<I18nString>> {


    public MultichoiceField() {
        super();
        super.setValue(new LinkedHashSet<I18nString>());
        super.setDefaultValue(new LinkedHashSet<I18nString>());
    }

    public MultichoiceField(List<I18nString> values) {
        super(values);
        super.setValue(new LinkedHashSet<I18nString>());
        super.setDefaultValue(new LinkedHashSet<I18nString>());
    }

    @Override
    public FieldType getType() {
        return FieldType.MULTICHOICE;
    }

    public void setDefaultValue(String value) {
        if (value == null) {
            super.setDefaultValue(null);
        } else {
            String[] vls = value.split(",");
            final LinkedHashSet<I18nString> defaults = new LinkedHashSet<I18nString>();
            Arrays.stream(vls).forEach(s -> {
                defaults.add(getChoices().stream().filter(it -> it.getDefaultValue().equals(s.trim())).findFirst().orElse(null));
            });
            super.setDefaultValue(defaults);
        }

    }

    public void setDefaultValues(List<String> inits) {
        if (inits == null || inits.isEmpty()) {
            super.setDefaultValue(null);
        } else {
            final LinkedHashSet<I18nString> defaults = new LinkedHashSet<I18nString>();
            inits.forEach(initValue -> {
                defaults.add(getChoices().stream().filter(choice -> choice.getDefaultValue().equals(initValue.trim())).findFirst().orElse(null));
            });
            super.setDefaultValue(defaults);
        }

    }

    public void setValue(final String value) {
        I18nString i18n = getChoices().stream().filter(it -> it.contains(value)).findFirst().orElse(null);
        if (i18n == null && value != null) {
            i18n = new I18nString(value);
        }
        super.setValue(new LinkedHashSet<>(Collections.singletonList(i18n)));
    }

    public void setValue(Collection<String> values) {
        LinkedHashSet<I18nString> newValues = new LinkedHashSet<>();
        for (String value : values) {
            I18nString i18n = getChoices().stream().filter(choice -> choice.contains(value)).findFirst().orElse(null);
            if (i18n == null && value != null) i18n = new I18nString(value);
            newValues.add(i18n);
        }
        super.setValue(newValues);
    }

    @Override
    public void setValue(LinkedHashSet<I18nString> value) {
        super.setValue(value);
    }

    @Override
    public Field<?> clone() {
        MultichoiceField clone = new MultichoiceField();
        super.clone(clone);
        clone.setChoices(this.getChoices());
        clone.choicesExpression = this.choicesExpression;
        return clone;
    }
}
