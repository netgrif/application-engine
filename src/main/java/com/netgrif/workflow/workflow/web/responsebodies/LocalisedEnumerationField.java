package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationField;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Data
public class LocalisedEnumerationField extends LocalisedField {

    private List<String> choices;

    private Object defaultValue;

    public LocalisedEnumerationField(EnumerationField field, Locale locale) {
        super(field, locale);
        this.choices = new LinkedList<>();
        Set<I18nString> choices = field.getChoices();
        for (I18nString choice : choices) {
            this.choices.add(choice.getTranslation(locale));
        }

        I18nString defaultI18n = field.getDefaultValue();
        if (defaultI18n != null)
            this.defaultValue = defaultI18n.getTranslation(locale);

        this.setValue(field.getTranslatedValue(locale));
    }
}
