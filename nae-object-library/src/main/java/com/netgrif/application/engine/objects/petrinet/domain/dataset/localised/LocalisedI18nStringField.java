package com.netgrif.application.engine.objects.petrinet.domain.dataset.localised;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.I18nField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalisedI18nStringField extends LocalisedField {

    public LocalisedI18nStringField(I18nField field, Locale locale) {
        super(field, locale);
        this.setValue(field.getValue().getTranslation(locale));
    }
}
