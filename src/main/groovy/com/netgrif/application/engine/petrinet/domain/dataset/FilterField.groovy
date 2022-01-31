package com.netgrif.application.engine.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
class FilterField extends FieldWithAllowedNets<String> {

    /**
     * Serialized information necessary for the restoration of the advanced search frontend GUI.
     *
     * Backend shouldn't need to interact with this attribute
     */
    private Map<String, Object> filterMetadata

    FilterField() {
        super()
        allowedNets = new ArrayList<>()
        filterMetadata = new HashMap<>()
    }

    FilterField(List<String> allowedNets) {
        super(allowedNets)
        filterMetadata = new HashMap<>()
    }

    @Override
    FieldType getType() {
        return FieldType.FILTER
    }

    @Override
    Field clone() {
        FilterField clone = new FilterField()
        super.clone(clone)
        clone.filterMetadata = this.filterMetadata

        return clone
    }

    Map<String, Object> getFilterMetadata() {
        return filterMetadata
    }

    void setFilterMetadata(Map<String, Object> filterMetadata) {
        this.filterMetadata = filterMetadata
    }
}
