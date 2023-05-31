package com.netgrif.application.engine.validation.models.number;

import com.netgrif.application.engine.petrinet.domain.dataset.NumberField;
import com.netgrif.application.engine.validation.service.interfaces.Validation;
import org.springframework.stereotype.Component;

@Component
public class NegativeValidation implements Validation<NumberField> {
    public void validate(NumberField field) throws Exception {
//        Validation validation = (Validation) field.getValidations().get(getName());
//        if (validation == null) {
//            throw new Exception("TOTOK");
//        }

    }

    public String getName() {
        return "negative";
    }
}