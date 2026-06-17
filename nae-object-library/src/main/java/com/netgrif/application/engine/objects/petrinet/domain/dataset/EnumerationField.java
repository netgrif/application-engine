package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;

import java.util.List;
import java.util.Locale;

public class EnumerationField extends ChoiceField<I18nString> {

    public EnumerationField() {
        super();
    }

    public EnumerationField(List<I18nString> values) {
        super(values);
    }

    @Override
    public FieldType getType() {
        return FieldType.ENUMERATION;
    }

    @Override
    public void setValue(I18nString value) {
        super.setValue(value);
        //TODO: case save choices
//        if (!value || choices.find { (it == value) }) {
//            super.setValue(value)
//        } else {
//            throw new IllegalArgumentException("Value $value is not a choice")
//        }
    }

    public void setValue(final String value) {
        I18nString i18n = getChoices().stream().filter(it -> it.contains(value)).findFirst().orElse(null);
        if (i18n == null) {
            i18n = new I18nString(value);
        }
        super.setValue(i18n);
    }

    public void setDefaultValue(final String defaultValue) {
        I18nString value = getChoices().stream().filter(it-> it.contains(defaultValue)).findFirst().orElse(null);
        if (value == null && defaultValue !=null) {
            value = new I18nString(defaultValue);
        }
        this.setDefaultValue(value);
    }

    public String getTranslatedValue(Locale locale) {
        if (getValue() == null) {
            return null;
        }
        return getValue().getTranslation(locale);
    }

    @Override
    public Field<?> clone() {
        EnumerationField clone = new EnumerationField();
        super.clone(clone);
        clone.setChoices(this.getChoices());
        clone.choicesExpression = this.choicesExpression;
        return clone;
    }

}
