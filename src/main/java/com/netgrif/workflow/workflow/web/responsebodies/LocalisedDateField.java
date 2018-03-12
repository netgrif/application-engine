package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.DateField;
import lombok.Data;

import java.util.Locale;
import java.util.Map;

@Data
public class LocalisedDateField extends LocalisedField {

    private String minDate;

    private String maxDate;

    // ValidableField
    private String validationJS;

    private Map<String, Boolean> validationErrors;

    private Object defaultValue;

    public LocalisedDateField(DateField field, Locale locale) {
        super(field, locale);
        this.minDate = field.getMinDate();
        this.maxDate = field.getMaxDate();
        this.validationErrors = field.getValidationErrors();
        this.validationJS = field.getValidationJS();
        this.defaultValue = field.getDefaultValue();
    }
}
