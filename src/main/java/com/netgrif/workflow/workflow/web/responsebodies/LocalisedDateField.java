package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.DateField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.LocalizedValidation;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.Validation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Data
public class LocalisedDateField extends LocalisedField {

    private String minDate;

    private String maxDate;

    // ValidableField
    private List<LocalizedValidation> validations;

    private Object defaultValue;

    public LocalisedDateField(DateField field, Locale locale) {
        super(field, locale);
        this.minDate = field.getMinDate();
        this.maxDate = field.getMaxDate();
        List<LocalizedValidation> locVal = new ArrayList<LocalizedValidation>();
        for(Validation val:field.getValidations()){
            locVal.add(val.getLocalizedValidation(locale));
        }
        this.validations = locVal;
        this.defaultValue = field.getDefaultValue();
    }
}
