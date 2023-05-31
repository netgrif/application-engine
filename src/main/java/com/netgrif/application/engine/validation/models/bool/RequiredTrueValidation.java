package com.netgrif.application.engine.validation.models.bool;

import com.netgrif.application.engine.petrinet.domain.dataset.BooleanField;
import com.netgrif.application.engine.validation.service.interfaces.Validation;
import org.springframework.stereotype.Component;

@Component
public class RequiredTrueValidation implements Validation<BooleanField> {

    public void validate(BooleanField field) throws Exception {
        com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation validation = field.getValidations().get(getName());
        if (validation == null) {
            throw new Exception("TOTOK");
        }
        Boolean value = field.getRawValue();

        if (value == null) {
            throw new Exception("TOTOK");
        }

        if (!value) {
            throw new Exception("TOTOK");
        }

    }

    public String getName() {
        return "requiredtrue";
    }
}