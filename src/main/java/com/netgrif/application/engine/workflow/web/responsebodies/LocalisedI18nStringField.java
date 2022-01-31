package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.I18nField;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedI18nStringField extends LocalisedField {

    public LocalisedI18nStringField(I18nField field, Locale locale) {
        super(field, locale);
        this.setValue(field.getValue().getTranslation(locale));
    }
}
