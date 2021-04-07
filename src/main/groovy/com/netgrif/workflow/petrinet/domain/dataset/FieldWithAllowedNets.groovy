package com.netgrif.workflow.petrinet.domain.dataset

interface FieldWithAllowedNets {
    List<String> getAllowedNets()
    void setAllowedNets(Collection<String> allowedNets)
}