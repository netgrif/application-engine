package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.EnumerationMapField;

import java.util.Locale;

public class LocalisedEnumerationMapField extends LocalisedMapOptionsField<String> {

    private Object defaultValue;

    public LocalisedEnumerationMapField(EnumerationMapField field, Locale locale) {
        super(field, locale);

        this.defaultValue = field.getDefaultValue();
    }
}
