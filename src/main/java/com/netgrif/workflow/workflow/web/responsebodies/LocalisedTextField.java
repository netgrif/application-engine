package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.TextField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.LocalizedValidation;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.Validation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
public class LocalisedTextField extends ValidableLocalisedField<TextField> {

    private String subType;

    private Integer maxLength;

    private String formatting;

    private Object defaultValue;

    public LocalisedTextField(TextField field, Locale locale) {
        super(field, locale);
        this.subType = field.getSubType();
        this.maxLength = field.getMaxLength();
        this.formatting = field.getFormatting();
        this.defaultValue = field.getDefaultValue();
    }
}