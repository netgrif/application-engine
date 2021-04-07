package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class CaseField extends FieldWithDefault<List<String>> implements FieldWithAllowedNets {

    private List<String> allowedNets

    CaseField() {
        super()
        super.defaultValue = new ArrayList()
        allowedNets = new ArrayList<>()
        this.value = new ArrayList<>()
    }

    CaseField(List<String> allowedNets) {
        this()
        this.setAllowedNets(allowedNets)
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
        clone.allowedNets = new ArrayList<>(this.allowedNets)

        return clone
    }

    List<String> getAllowedNets() {
        return allowedNets
    }

    void setAllowedNets(Collection<String> allowedNets) {
        this.allowedNets.clear()
        this.allowedNets.addAll(allowedNets)
    }
}