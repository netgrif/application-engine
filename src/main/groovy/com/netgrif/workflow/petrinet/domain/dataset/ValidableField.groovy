package com.netgrif.workflow.petrinet.domain.dataset

import com.fasterxml.jackson.annotation.JsonIgnore
import com.netgrif.workflow.petrinet.domain.I18nString
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.Validation
import org.springframework.data.mongodb.core.mapping.Document

@Document
abstract class ValidableField<T> extends FieldWithDefault<T> {

    private List<Validation> validations

    ValidableField() {
        super()
    }

    void addValidation(Validation validation) {
        if (validations == null) {
            this.validations = new ArrayList<Validation>()
        }
        this.validations.add(validation)
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
