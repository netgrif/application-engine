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

    void execute(Case useCase, Field<?> field) {
        List<Validation> validations = field.getValidations()
        if (!validations) {
            return
        }

        log.info("Validations: ${validations.collect { it.name }}")

        ValidationDelegate delegate = initDelegate(useCase, field, validations.collect { it.name })
        // TODO: release/8.0.0 required fields, null raw value
        validations.each { validation ->
            runValidation(validation, delegate)
        }
    }

    protected void runValidation(Validation validation, ValidationDelegate delegate) {
        Closure<Boolean> code = initCode(validation, delegate)
        if (!code()) {
            throw new IllegalArgumentException(validation.message.toString())
        }
    }

    protected Closure<Boolean> getValidationCode(String validationName) {
        return this.registry.getValidation(validationName)
    }

    protected static String escapeSpecialCharacters(String s) {
        return s.replace('\\', '\\\\')
                .replace('\'', '\\\'')
    }

    protected Closure<Boolean> initCode(Validation validation, ValidationDelegate delegate) {
        List<String> argumentList = []
        if (validation.serverArguments != null) {
            argumentList = validation.serverArguments.argument.collect { it.isDynamic() ? it.definition : "'${escapeSpecialCharacters(it.defaultValue)}'" }
        }
        String validationCall = "${validation.name}(${argumentList.join(", ")})"
        Closure<Boolean> code = this.shellFactory.getGroovyShell().evaluate("{ -> return " + validationCall + " }") as Closure<Boolean>
        return code.rehydrate(delegate, code.owner, code.thisObject)
    }

    protected ValidationDelegate initDelegate(Case useCase, Field<?> thisField, List<String> validationNames) {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.metaClass.useCase = useCase
        useCase.dataSet.fields.values().forEach { Field<?> field ->
            delegate.metaClass."$field.importId" = field
        }

        validationNames = filterConflictedValidationNames(useCase, validationNames)
        validationNames.each { validationName ->
            delegate.metaClass."$validationName" = getValidationCode(validationName)
        }

        delegate.field = thisField
        return delegate
    }

    private static List<String> filterConflictedValidationNames(Case useCase, List<String> validationNames) {
        List<String> fieldNames = useCase.dataSet.fields.keySet() as List<String>
        fieldNames.retainAll(validationNames)
        if (!fieldNames.isEmpty()) {
            log.warn("Ignoring validations {} for case [{}]: field names are identical with validation names", fieldNames, useCase.stringId)
            validationNames -= fieldNames
        }
        return validationNames
    }
}