package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationField;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedEnumerationField extends LocalisedChoiceField {

    public LocalisedEnumerationField(EnumerationField field, Locale locale) {
        super(field, locale);
        this.setValue(field.getTranslatedValue(locale));
    }
}
