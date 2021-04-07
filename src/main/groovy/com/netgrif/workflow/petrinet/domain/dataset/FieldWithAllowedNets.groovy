package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
abstract class FieldWithAllowedNets<T> extends FieldWithDefault<T> {

    private List<String> allowedNets

    FieldWithAllowedNets() {
        super()
        allowedNets = new ArrayList<>()
    }

    FieldWithAllowedNets(List<String> allowedNets) {
        this()
        this.setAllowedNets(allowedNets)
    }

    @Override
    void clone(Field clone) {
        super.clone(clone)
        ((FieldWithAllowedNets) clone).allowedNets = new ArrayList<>(this.allowedNets)
    }

    List<String> getAllowedNets() {
        return allowedNets
    }

    void setAllowedNets(Collection<String> allowedNets) {
        this.allowedNets.clear()
        this.allowedNets.addAll(allowedNets)
    }
}