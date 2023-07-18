package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceMapField;

import java.util.LinkedHashSet;
import java.util.Locale;

public class LocalisedMultichoiceMapField extends LocalisedMapOptionsField<LinkedHashSet<String>> {

    public LocalisedMultichoiceMapField(MultichoiceMapField field, Locale locale) {
        super(field, locale);
    }
}
