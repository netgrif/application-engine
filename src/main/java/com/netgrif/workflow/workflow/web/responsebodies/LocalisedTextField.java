package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.TextField;
import lombok.Data;

import java.util.Locale;
import java.util.Map;

@Data
public class LocalisedTextField extends LocalisedField {

    private String subType;

    private Integer maxLength;

    private String formatting;

    private String validationJS;

    private Map<String, Boolean> validationErrors;

    private Object defaultValue;

    public LocalisedTextField(TextField field, Locale locale) {
        super(field, locale);
        this.subType = field.getSubType();
        this.maxLength = field.getMaxLength();
        this.formatting = field.getFormatting();
        this.validationErrors = field.getValidationErrors();
        this.validationJS = field.getValidationJS();
        this.defaultValue = field.getDefaultValue();
    }
}