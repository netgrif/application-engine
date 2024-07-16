package com.netgrif.application.engine.petrinet.domain.dataset.logic.action

import com.netgrif.application.engine.event.IGroovyShellFactory
import com.netgrif.application.engine.petrinet.domain.dataset.ArgumentsType
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.dataset.Validation
import com.netgrif.application.engine.validations.ValidationRegistry
import com.netgrif.application.engine.workflow.domain.Case
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.stereotype.Component

@Slf4j
@Component
abstract class ValidationExecutioner {

    @Lookup("validationDelegate")
    abstract ValidationDelegate getValidationDelegate()

    @Autowired
    private ValidationRegistry registry;

    @Autowired
    private IGroovyShellFactory shellFactory

    void execute(Case useCase, Field<?> field, List<Validation> validations) {
        if (validations) {
            log.info("Validations: ${validations.collect { it.name }}")

            ValidationDelegate delegate = initDelegate(useCase, field, validations.collect { it.name })
            for (Validation validation : validations) {
                List<String> argumentList = []
                if (validation.arguments != null) {
                    if (validation.arguments.type != ArgumentsType.SERVER) {
                        continue
                    }
                    argumentList = validation.arguments.argument
                }
//                Closure<Boolean> code = initCode(validation, delegate)
                if (!delegate."${validation.name}"(*argumentList)) {
                    throw new IllegalArgumentException(validation.message.toString())
                }
            }
        }
    }

    protected Closure<Boolean> getValidationCode(String validationName) {
        return this.registry.getValidation(validationName)
    }

    protected Closure<Boolean> initCode(Validation validation, ValidationDelegate delegate) {
        List arguments = []
        if (validation.arguments != null) {
            arguments = validation.arguments.argument
        }
        String validationCall = "${validation.name}(${arguments.join(", ")})"
        Closure<Boolean> code = this.shellFactory.getGroovyShell().evaluate("{ -> return " + validationCall + " }") as Closure<Boolean>
        return code.rehydrate(delegate, code.owner, code.thisObject)
    }

    protected ValidationDelegate initDelegate(Case useCase, Field<?> thisField, List<String> validationNames) {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.metaClass.useCase = useCase
        useCase.dataSet.fields.values().forEach { Field<?> field ->
            delegate.metaClass."$field.importId" = field
        }

        Set commonFieldValidationNames = useCase.dataSet.fields.keySet()
        commonFieldValidationNames.retainAll(validationNames)
        if (!commonFieldValidationNames.isEmpty()) {
            log.warn("Ignoring validations {} for case [{}]: field names are identical with validation names", commonFieldValidationNames, useCase.stringId)
            validationNames -= commonFieldValidationNames
        }
        validationNames.forEach { validationName ->
            delegate.metaClass."$validationName" = getValidationCode(validationName)
        }
        delegate.thisField = thisField
        return delegate
    }
}
