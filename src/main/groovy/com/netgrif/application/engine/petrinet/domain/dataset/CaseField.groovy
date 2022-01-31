package com.netgrif.application.engine.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class CaseField extends FieldWithAllowedNets<List<String>> {

    CaseField() {
        this(new ArrayList<String>())
    }

    CaseField(List<String> allowedNets) {
        super(allowedNets)
        super.defaultValue = new ArrayList()
    }

    @Override
    FieldType getType() {
        return FieldType.CASE_REF
    }

    @Override
    void clearValue() {
        this.setValue(new ArrayList<String>())
    }

    @Override
    Field clone() {
        CaseField clone = new CaseField()
        super.clone(clone)
        return clone
    }
}