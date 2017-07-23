package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class CaseField extends Field<String> {

    private Long[] constraintNetIds

    CaseField(Long[] netId) {
        super()
        constraintNetIds = netId
    }

    @Override
    void setValue(String value) {
        super.setValue(value)
    }

    @Override
    void clearValue() {
        super.clearValue()
    }

    Long[] getConstraintNetId() {
        return constraintNetIds
    }

    void setConstraintNetId(Long[] constraintNetId) {
        this.constraintNetIds = constraintNetId
    }
}