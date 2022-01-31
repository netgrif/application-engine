package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField;

import java.util.Locale;

public class LocalisedEnumerationMapField extends LocalisedMapOptionsField<String> {

    public LocalisedEnumerationMapField(EnumerationMapField field, Locale locale) {
        super(field, locale);
    }
}
