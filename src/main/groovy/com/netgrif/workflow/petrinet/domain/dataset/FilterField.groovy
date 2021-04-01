package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class FilterField extends FieldWithDefault<String> {

    private List<String> allowedNets
    /**
     * Serialized information necessary for the restoration of the advanced search frontend GUI.
     *
     * Backend shouldn't need to interact with this attribute
     */
    private Object filterMetadata

    FilterField() {
        super()
        allowedNets = new ArrayList<>()
    }

    FilterField(List<String> allowedNets) {
        this()
        this.setAllowedNets(allowedNets)
    }

    @Override
    FieldType getType() {
        return FieldType.FILTER
    }

    @Override
    Field clone() {
        FilterField clone = new FilterField()
        super.clone(clone)
        clone.allowedNets = new ArrayList<>(this.allowedNets)
        clone.filterMetadata = this.filterMetadata

        return clone
    }

    List<String> getAllowedNets() {
        return allowedNets
    }

    void setAllowedNets(List<String> allowedNets) {
        this.allowedNets.clear()
        this.allowedNets.addAll(allowedNets)
    }
}
