package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.BooleanField;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedBooleanField extends LocalisedField {

    private Boolean defaultValue;

    public LocalisedBooleanField(BooleanField field, Locale locale) {
        super(field, locale);
        this.defaultValue = field.getDefaultValue();
    }
}
