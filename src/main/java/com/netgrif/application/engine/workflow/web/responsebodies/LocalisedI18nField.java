package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.core.petrinet.domain.dataset.I18nField;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedI18nField extends LocalisedField {

    public LocalisedI18nField(I18nField field, Locale locale) {
        super(field, locale);
    }
}
