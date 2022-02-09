package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.BooleanField;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedBooleanField extends LocalisedField {

    public LocalisedBooleanField(BooleanField field, Locale locale) {
        super(field, locale);
    }
}
