package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.BooleanField;
import com.netgrif.workflow.petrinet.domain.dataset.ValidableField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.LocalizedValidation;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.Validation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
public class LocalisedBooleanField extends ValidableLocalisedField<BooleanField> {

    private Boolean defaultValue;

    public LocalisedBooleanField(BooleanField field, Locale locale) {
        super(field, locale);
        this.defaultValue = field.getDefaultValue();
    }
}
