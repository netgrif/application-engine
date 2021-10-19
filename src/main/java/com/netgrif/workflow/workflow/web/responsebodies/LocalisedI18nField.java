package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.I18nField;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedI18nField extends LocalisedField {

    public LocalisedI18nField(I18nField field, Locale locale) {
        super(field, locale);
        this.setValue(field.getValue().getTranslation(locale));
    }
}
