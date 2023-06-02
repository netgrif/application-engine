package com.netgrif.application.engine.validation.service;

import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.validation.validator.IValidator;
import com.netgrif.application.engine.workflow.domain.DataField;
import com.netgrif.application.engine.workflow.domain.Task;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
class ValidationRegistryService implements IValidationRegistryService {
    private final Map<String, IValidator<?>> registry;

    public ValidationRegistryService(List<IValidator<?>> validators) {
        this.registry = validators.stream().collect(Collectors.toMap(IValidator::getName, Function.identity()));
    }

    @Override
    public void validate(Task task) {

    }

    @Override
    public <T extends Field<?>> void validate(T field, DataField dataField) {
        if (field.getValidations() == null) {
            return;
        }
        field.getValidations().forEach(validation -> {
            IValidator<T> validator = (IValidator<T>) registry.get(validation.getName());
            try {
                validator.validate(field, dataField);
            } catch (ValidationException | ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
