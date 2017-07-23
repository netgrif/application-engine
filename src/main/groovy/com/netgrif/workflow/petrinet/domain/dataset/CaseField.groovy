package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document

@Document
class CaseField extends Field<String> {

    /**
     * Stores PetriNet import id and immediate fields ids of given net
     */
    private Map<Long, LinkedHashSet<Long>> constraintNetIds

    @Transient
    private Map<String, Object> immediateFieldValues

    CaseField() {
        super()
        immediateFieldValues = new HashMap<>()
    }

    CaseField(Map<Long, LinkedHashSet<Long>> netId) {
        this()
        constraintNetIds = netId
    }

    @Override
    void setValue(String value) {
        super.setValue(value)
    }

    @Override
    void clearValue() {
        this.setValue(null)
    }

    Map<Long, Set<Long>> getConstraintNetIds() {
        return constraintNetIds
    }

    void setConstraintNetIds(Map<Long, LinkedHashSet<Long>> constraintNetId) {
        this.constraintNetIds = constraintNetId
    }

    Map<String, Object> getImmediateFieldValues() {
        return immediateFieldValues
    }

    void setImmediateFieldValues(Map<String, Object> immediateFieldValues) {
        this.immediateFieldValues = immediateFieldValues
    }
}