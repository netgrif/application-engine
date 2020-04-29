package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.NumberField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.LocalizedValidation;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.Validation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Data
public class LocalisedNumberField extends LocalisedField {

    private Double minValue;

    private Double maxValue;

    private List<LocalizedValidation> validations;

    private Object defaultValue;

    public LocalisedNumberField(NumberField field, Locale locale) {
        super(field, locale);
        this.minValue = field.getMinValue();
        this.maxValue = field.getMaxValue();
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
