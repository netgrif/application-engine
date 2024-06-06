package com.netgrif.application.engine.petrinet.domain.dataset.logic.action


import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.dataset.Validation
import com.netgrif.application.engine.validations.ValidationRegistry
import com.netgrif.application.engine.validations.ValidationService
import com.netgrif.application.engine.workflow.domain.Case
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.stereotype.Component

@Slf4j
@Component
abstract class ValidationRunner {

    @Lookup("validationDelegate")
    abstract ValidationDelegate getValidationDelegate()

    @Autowired
    private ValidationRegistry validationRegistry;

    @Autowired
    private ValidationService service

    void run(Case useCase, List<Validation> validations) {
        if (validations) {
            log.debug("Validations: ${validations.collect {it.rule }}")
            validations.each { Validation validation ->
                String validationName, validationArgs
                (validationName, validationArgs) = validation.rule.trim().split(" ")
                Closure code = getValidationCode(validationName)
                initCode(code.delegate, useCase)
                if (!code(*validationArgs.split(","))) {
                    throw new IllegalArgumentException(validation.message.toString())
                }
            }
        }
    }

    protected Closure<Boolean> getValidationCode(String validationName) {
        Closure<Boolean> code = validationRegistry.getValidation(validationName)
        return code.rehydrate(getValidationDelegate(), code.owner, code.thisObject)
    }

    protected static void initCode(def delegate, Case useCase) {
        delegate.metaClass.useCase = useCase
        useCase.dataSet.fields.values().forEach { Field<?> field ->
            delegate.metaClass."$field.importId" = field
        }
    }
}
