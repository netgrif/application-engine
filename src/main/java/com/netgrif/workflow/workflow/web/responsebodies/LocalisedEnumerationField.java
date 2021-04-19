package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationField;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Data
public class LocalisedEnumerationField extends LocalisedChoiceField {

    public LocalisedEnumerationField(EnumerationField field, Locale locale) {
        super(field, locale);
        I18nString defaultI18n = field.getDefaultValue();
        if (defaultI18n != null)
            this.setDefaultValue(defaultI18n.getTranslation(locale));

        this.setValue(field.getTranslatedValue(locale));
    }
}
