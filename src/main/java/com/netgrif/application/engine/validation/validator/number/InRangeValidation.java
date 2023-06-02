package com.netgrif.application.engine.validation.validator.number;

import com.netgrif.application.engine.petrinet.domain.dataset.NumberField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.validation.validator.IValidator;
import com.netgrif.application.engine.workflow.domain.DataField;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InRangeValidation implements IValidator<NumberField> {

    static final String INF = "inf";

    @Override
    public void validate(NumberField field, DataField dataField) throws ValidationException {
        Optional<Validation> possibleValidation = field.getValidations().stream().filter(v -> v.getName().equals(getName())).findFirst();
        if (possibleValidation.isEmpty()) {
            return;
        }
        Validation validation = possibleValidation.get();
        String from = validation.getArguments().get("from").getValue();
        String to = validation.getArguments().get("to").getValue();
        Double value = (Double) dataField.getValue();

        if (value == null) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value is NULL");
        }

        if (from.equals(INF) && value > Double.parseDouble(to)) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value [" + value + "] should be in range [" + from + ", " + to + "]" );
        }

        if (value < Double.parseDouble(from) && to.equals(INF)) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value [" + value + "] should be in range [" + from + ", " + to + "]" );
        }

        if (!(Double.parseDouble(from) <= value && value <= Double.parseDouble(to))) {
            throw new ValidationException("Invalid value of field [" + field.getImportId() + "], value [" + value + "] should be in range [" + from + ", " + to + "]" );
        }
    }

    public String getName() {
        return "inrange";
    }
}