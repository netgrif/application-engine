package com.netgrif.workflow.petrinet.domain.dataset

import com.fasterxml.jackson.annotation.JsonIgnore
import com.netgrif.workflow.petrinet.domain.I18nString
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.Validation
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document

@Document
abstract class ValidableField<T> extends FieldWithDefault<T> {

    private List<Validation> validations

    ValidableField() {
        super()
    }

    void addValidation(String validationRule,I18nString validationMessage){
        Validation add = new Validation()
        if(validationMessage == null) add = new Validation(validationRule)
        else add = new Validation(validationRule,validationMessage)
        if(validations == null){
            this.validations = new ArrayList<Validation>()
        }
        this.validations.add(add)
    }

    List<Validation> getValidations() {
        return validations
    }

    void setValidations(List<Validation> validations) {
        this.validations = validations
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
