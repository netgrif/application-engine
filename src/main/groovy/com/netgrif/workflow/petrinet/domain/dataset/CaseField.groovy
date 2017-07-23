package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class CaseField extends Field<String> {

    /**
     * Stores PetriNet import id and immediate fields ids of given net
     */
    private Map<Long, Set<Long>> constraintNetIds

    CaseField() {
        super()
    }

    CaseField(Map<Long, Set<Long>> netId) {
        this()
        constraintNetIds = netId
    }

    @Override
    void setValue(String value) {
        this.value = value
    }

    @Override
    void clearValue() {
        this.value = null
    }

    Map<Long, Set<Long>> getConstraintNetId() {
        return constraintNetIds
    }

    void setConstraintNetId(Map<Long, Set<Long>> constraintNetId) {
        this.constraintNetIds = constraintNetId
    }
}