package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.BooleanField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.LocalizedValidation;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.Validation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
public class LocalisedBooleanField extends LocalisedField {

    private Boolean defaultValue;

    private List<LocalizedValidation> validations;

    public LocalisedBooleanField(BooleanField field, Locale locale) {
        super(field, locale);
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
