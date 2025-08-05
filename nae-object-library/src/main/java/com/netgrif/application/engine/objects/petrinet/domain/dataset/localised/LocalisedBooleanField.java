package com.netgrif.application.engine.objects.petrinet.domain.dataset.localised;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.BooleanField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalisedBooleanField extends LocalisedField {

    public LocalisedBooleanField(BooleanField field, Locale locale) {
        super(field, locale);
    }
}
