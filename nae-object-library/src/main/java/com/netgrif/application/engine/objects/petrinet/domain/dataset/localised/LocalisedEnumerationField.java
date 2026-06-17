package com.netgrif.application.engine.objects.petrinet.domain.dataset.localised;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.EnumerationField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalisedEnumerationField extends LocalisedChoiceField {

    public LocalisedEnumerationField(EnumerationField field, Locale locale) {
        super(field, locale);
        this.setValue(field.getTranslatedValue(locale));
    }
}
