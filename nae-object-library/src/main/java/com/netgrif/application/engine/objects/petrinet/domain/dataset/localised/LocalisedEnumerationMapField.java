package com.netgrif.application.engine.objects.petrinet.domain.dataset.localised;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.EnumerationMapField;

import java.util.Locale;

public class LocalisedEnumerationMapField extends LocalisedMapOptionsField<String> {

    public LocalisedEnumerationMapField(EnumerationMapField field, Locale locale) {
        super(field, locale);
    }
}
