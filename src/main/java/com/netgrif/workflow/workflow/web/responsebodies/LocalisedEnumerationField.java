package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationField;
import lombok.Data;

import java.util.*;

@Data
public class LocalisedEnumerationField extends LocalisedField {

    private Map<String, String> choices;

    private Object defaultValue;

    public LocalisedEnumerationField(EnumerationField field, Locale locale) {
        super(field, locale);
        this.choices = new LinkedHashMap<>();
        Map<String, I18nString> choices = field.getChoices();
        for (Map.Entry<String, I18nString> choice : choices.entrySet()) {
            this.choices.put(choice.getKey(), choice.getValue().getTranslation(locale));
        }

        I18nString defaultI18n = field.getDefaultValue();
        if (defaultI18n != null)
            this.defaultValue = defaultI18n.getTranslation(locale);

        this.setValue(field.getTranslatedValue(locale));
    }
}
