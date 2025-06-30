package com.netgrif.application.engine.objects.petrinet.domain.dataset.localised;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.BooleanField;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedBooleanField extends LocalisedField {

    public LocalisedBooleanField(BooleanField field, Locale locale) {
        super(field, locale);
    }
}
