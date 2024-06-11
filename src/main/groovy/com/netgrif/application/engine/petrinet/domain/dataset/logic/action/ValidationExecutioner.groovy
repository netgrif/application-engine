package com.netgrif.application.engine.petrinet.domain.dataset.logic.action

import com.netgrif.application.engine.event.IGroovyShellFactory
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

    void run(Case useCase, Field<?> field, List<Validation> validations) {
        if (validations) {
            log.info("Validations: ${validations.collect {it.rule }}")

            def delegate = getValidationDelegate()

            initCode(delegate, useCase, field, this.registry.getValidationNames())
            for (Validation validation : validations) {
                Closure<Boolean> code = (Closure<Boolean>) this.shellFactory.getGroovyShell().evaluate("{ -> " + validation.rule + "}")
                code = code.rehydrate(delegate, code.owner, code.thisObject)
                if (!code()) {
                    throw new IllegalArgumentException(validation.message.toString())
                }
            }
        }
    }

    protected Closure<Boolean> getValidationCode(String validationName, Field<?> thisField) {
        Closure<Boolean> code = this.registry.getValidation(validationName)
        code.delegate.metaClass.thisField = thisField
        return code.rehydrate(code.delegate, code.owner, code.thisObject)
    }

    protected void initCode(def delegate, Case useCase, Field<?> thisField, List<String> validationNames) {
        delegate.metaClass.useCase = useCase
        useCase.dataSet.fields.values().forEach { Field<?> field ->
            delegate.metaClass."$field.importId" = field
        }
        validationNames.forEach { validationName ->
            delegate.metaClass."$validationName" = getValidationCode(validationName, thisField)
        }
    }
}
