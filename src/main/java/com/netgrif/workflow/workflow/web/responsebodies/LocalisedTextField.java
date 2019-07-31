package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.TextField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.LocalizedValidation;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.Validation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Data
public class LocalisedTextField extends LocalisedField {

    private String subType;

    private Integer maxLength;

    private String formatting;

    private List<LocalizedValidation> validations;

    private Object defaultValue;

    public LocalisedTextField(TextField field, Locale locale) {
        super(field, locale);
        this.subType = field.getSubType();
        this.maxLength = field.getMaxLength();
        this.formatting = field.getFormatting();
        List<LocalizedValidation> locVal = new ArrayList<LocalizedValidation>();
        for(Validation val:field.getValidations()){
            locVal.add(val.getLocalizedValidation(locale));
        }
        this.validations = locVal;
        this.defaultValue = field.getDefaultValue();
    }
}