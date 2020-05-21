package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.DateTimeField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.LocalizedValidation;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.Validation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
public class LocalisedDateTimeField extends LocalisedField {

    private String minDate;

    private String maxDate;

    // ValidableField
    private List<LocalizedValidation> validations;

    private Object defaultValue;

    public LocalisedDateTimeField(DateTimeField field, Locale locale) {
        super(field, locale);
        this.minDate = field.getMinDate();
        this.maxDate = field.getMaxDate();
        List<LocalizedValidation> locVal = new ArrayList<LocalizedValidation>();
        if (field.getValidations() != null) {
            for(Validation val:field.getValidations()){
                locVal.add(val.getLocalizedValidation(locale));
            }
        }
        this.validations = locVal;
        this.defaultValue = field.getDefaultValue();
    }
}
