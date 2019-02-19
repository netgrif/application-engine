package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.NumberField;
import lombok.Data;

import java.util.Locale;
import java.util.Map;

@Data
public class LocalisedNumberField extends LocalisedField {

    private Double minValue;

    private Double maxValue;

    private String validationJS;

    private Map<String, Boolean> validationErrors;

    private Object defaultValue;

    public LocalisedNumberField(NumberField field, Locale locale) {
        super(field, locale);
        this.minValue = field.getMinValue();
        this.maxValue = field.getMaxValue();
        this.validationErrors = field.getValidationErrors();
        this.validationJS = field.getValidationJS();
        this.defaultValue = field.getDefaultValue();
    }
}
