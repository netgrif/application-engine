package com.netgrif.workflow.petrinet.domain.dataset

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document

@Document
abstract class ValidableField<T> extends FieldWithDefault<T> {

    @JsonIgnore
    private String validationRules

    @Transient
    private String validationJS

    @Transient
    private Map<String, Boolean> validationErrors

    ValidableField() {
        super()
    }

    String getValidationRules() {
        return validationRules
    }

    void setValidationRules(String rules) {
        this.validationRules = rules
    }

    void setValidationRules(List<String> rules) {
        if (rules == null || rules.empty)
            return

        StringBuilder builder = new StringBuilder()
        rules.each { rule ->
            rule = rule.trim()
            if (rule.contains(" ") || rule.contains("(")) builder.append("{${rule}},")
            else builder.append(rule + ",")
        }
        builder.deleteCharAt(builder.length() - 1)
        this.validationRules = builder.toString()
    }

    String getValidationJS() {
        return validationJS
    }

    void setValidationJS(String validationJS) {
        this.validationJS = validationJS
    }

    Map<String, Boolean> getValidationErrors() {
        return validationErrors
    }

    void setValidationErrors(Map<String, Boolean> validationErrors) {
        this.validationErrors = validationErrors
    }

    void addValidationError(String key, Boolean value) {
        if (this.validationErrors == null) this.validationErrors = new HashMap<>()
        this.validationErrors.put(key, value)
    }

    void addValidationError(String key) {
        this.addValidationError(key, false)
    }

    @JsonIgnore
    T superGetDefaultValue() {
        return super.defaultValue
    }

    @JsonIgnore
    void superSetDefaultValue(T value) {
        super.defaultValue = value
    }
}
