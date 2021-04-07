package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class CaseField extends FieldWithAllowedNets<List<String>>  {

    CaseField() {
        super()
        super.defaultValue = new ArrayList()
    }

    CaseField(List<String> allowedNets) {
        super(allowedNets)
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