package com.netgrif.application.engine.validations;

import com.netgrif.application.engine.event.IGroovyShellFactory;
import com.netgrif.application.engine.petrinet.domain.Transition;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ValidationExecutioner;
import com.netgrif.application.engine.validations.interfaces.IValidationService;
import com.netgrif.application.engine.workflow.domain.Case;
import groovy.lang.Closure;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ValidationService implements IValidationService {

    private final ValidationRegistry validationRegistry;

    private final ValidationExecutioner validationExecutioner;

    private final IGroovyShellFactory shellFactory;

    @Autowired
    public ValidationService(ValidationRegistry validationRegistry, ValidationExecutioner validationExecutioner, IGroovyShellFactory shellFactory) {
        this.validationRegistry = validationRegistry;
        this.validationExecutioner = validationExecutioner;
        this.shellFactory = shellFactory;
    }

    @Override
    public void validateTransition(Case useCase, Transition transition) {
        transition.getDataSet().values().forEach(dataRef -> {
            if (dataRef.getField() != null) {
                validationExecutioner.execute(useCase, dataRef.getField(), dataRef.getField().getValidations());
            }
        });
    }

    @Override
    public void validateField(Case useCase, Field<?> field) {
        validationExecutioner.execute(useCase, field, field.getValidations());
    }

    @Override
    public void registerValidation(String name, String definition) throws ClassCastException, CompilationFailedException {
        Closure<Boolean> code = (Closure<Boolean>) this.shellFactory.getGroovyShell().evaluate("{" + definition + "}");
        validationRegistry.addValidation(name, code);
    }

    @Override
    public Closure<Boolean> getValidation(String name) {
        return validationRegistry.getValidation(name);
    }

    @Override
    public void unregisterValidation(String name) {
        validationRegistry.removeValidation(name);
    }

    @Override
    public void clearValidations() {
        validationRegistry.removeAllValidations();
    }
}
