package com.netgrif.application.engine.validations.interfaces;

import com.netgrif.application.engine.petrinet.domain.Transition;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.workflow.domain.Case;
import groovy.lang.Closure;
import org.codehaus.groovy.control.CompilationFailedException;

public interface IValidationService {

    void validateTransition(Case useCase, Transition transition);

    void validateField(Case useCase, Field<?> field);

    void registerValidation(String name, String definition) throws ClassCastException, CompilationFailedException;

    Closure<Boolean> getValidation(String name);

    void unregisterValidation(String name);
}
