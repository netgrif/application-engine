package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceMapField;

import java.util.Locale;
import java.util.Set;

public class LocalisedMultichoiceMapField extends LocalisedMapOptionsField<Set<String>> {

    public LocalisedMultichoiceMapField(MultichoiceMapField field, Locale locale) {
        super(field, locale);
    }
}
