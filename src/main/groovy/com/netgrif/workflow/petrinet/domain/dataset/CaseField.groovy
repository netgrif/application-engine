package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class CaseField extends Field<Set<String>> {

    private Set<String> allowedNets

    CaseField() {
        super()
        allowedNets = new LinkedHashSet<>()
    }

    CaseField(Set<String> allowedNets) {
        this()
        this.setAllowedNets(allowedNets)
    }

    @Override
    FieldType getType() {
        return FieldType.CASE_REF
    }

    @Override
    void clearValue() {
        this.setValue(new LinkedHashSet<String>())
    }

    @Override
    Field clone() {
        CaseField clone = new CaseField()
        super.clone(clone)
        clone.allowedNets = this.allowedNets

        return clone
    }

    Set<String> getAllowedNets() {
        return allowedNets
    }

    void setAllowedNets(Set<String> allowedNets) {
        this.allowedNets.clear()
        allowedNets.each {this.allowedNets.add(it)}
    }
}