package com.netgrif.application.engine.validation.models.text;

import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.validation.service.interfaces.Validation;
import org.springframework.stereotype.Component;

@Component
public class TelnumberValidation implements Validation<TextField> {

    public static String telNumberRegex = "^(?:\\+?(\\d{1,3}))?([-. (]*(\\d{3})[-. )]*)?((\\d{3})[-. ]*(\\d{2,4})(?:[-.x ]*(\\d+))?)$";

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
        return "telnumber";
    }
}