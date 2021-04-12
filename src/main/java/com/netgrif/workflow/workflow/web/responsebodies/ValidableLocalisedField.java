package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.ValidableField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.LocalizedValidation;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
@NoArgsConstructor
public class ValidableLocalisedField<T extends ValidableField<?>> extends LocalisedField {

    protected List<LocalizedValidation> validations;

    public ValidableLocalisedField(T field, Locale locale) {
        super(field, locale);
        List<LocalizedValidation> locVal = new ArrayList<>();
        if (field.getValidations() != null) {
            field.getValidations().forEach(val -> locVal.add(val.getLocalizedValidation(locale)));
        }
        this.validations = locVal;
    }
}
