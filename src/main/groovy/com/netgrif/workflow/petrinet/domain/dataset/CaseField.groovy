package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document

@Document
class CaseField extends Field<String> {

    /**
     * Stores PetriNet object id and immediate fields ids of given net
     */
    private Map<String, LinkedHashSet<String>> constraintNetIds

    @Transient
    private Map<String, Object> immediateFieldValues

    CaseField() {
        super()
        immediateFieldValues = new HashMap<>()
    }

    CaseField(Map<String, LinkedHashSet<String>> netId) {
        this()
        constraintNetIds = netId
    }

    @Override
    FieldType getType() {
        return FieldType.CASEREF
    }

    @Override
    void clearValue() {
        this.setValue(null)
    }

    @Override
    Field clone() {
        CaseField clone = new CaseField()
        super.clone(clone)
        clone.constraintNetIds = this.constraintNetIds

        return clone
    }

    Map<String, LinkedHashSet<String>> getConstraintNetIds() {
        return constraintNetIds
    }

    void setConstraintNetIds(Map<String, LinkedHashSet<String>> constraintNetId) {
        this.constraintNetIds = constraintNetId
    }

    Map<String, Object> getImmediateFieldValues() {
        return immediateFieldValues
    }

    void setImmediateFieldValues(Map<String, Object> immediateFieldValues) {
        this.immediateFieldValues = immediateFieldValues
    }
}