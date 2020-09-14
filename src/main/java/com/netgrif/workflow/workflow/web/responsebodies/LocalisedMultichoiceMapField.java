package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.MultichoiceMapField;

import java.util.Locale;
import java.util.Set;

public class LocalisedMultichoiceMapField extends LocalisedMapOptionsField<Set<String>> {

    private Object defaultValue;

    public LocalisedMultichoiceMapField(MultichoiceMapField field, Locale locale) {
        super(field, locale);

        this.defaultValue = field.getDefaultValue();
    }
}
