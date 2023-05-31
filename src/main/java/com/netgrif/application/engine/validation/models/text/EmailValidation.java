package com.netgrif.application.engine.validation.models.text;

import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.validation.service.interfaces.Validation;
import org.springframework.stereotype.Component;

@Component
public class EmailValidation implements Validation<TextField> {

    public static String emailRegex = "^[a-zA-Z0-9\\._\\%\\+\\-]+@[a-zA-Z0-9\\.\\-]+\\.[a-zA-Z]{2,}$";

    public void validate(TextField field) throws Exception {
        com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation validation = field.getValidations().get(getName());
        if (validation == null) {
            throw new Exception("TOTOK");
        }
//
//        String pattern = validation.getValidationRule("expression"); // {0-9}[5]
//        String value = field.getRawValue();
//
//        if (value == null) {
//            throw new Exception("TOTOK");
//        }
//
//        if (!value.matches(pattern)) {
//            throw new Exception("TOTOK");
//        }

    }

    public String getName() {
        return "email";
    }
}