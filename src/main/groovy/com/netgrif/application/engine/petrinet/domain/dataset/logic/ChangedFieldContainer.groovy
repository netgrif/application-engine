package com.netgrif.application.engine.petrinet.domain.dataset.logic

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class ChangedFieldContainer {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Map<String, Object>> changedFields

    ChangedFieldContainer() {
        changedFields = new HashMap<>()
    }

    void putAll(Map<String, ChangedField> changed) {
        changed.each { key, field -> changedFields.put(field.id, field.attributes) }
    }

    Map<String, Map<String, Object>> getChangedFields() {
        return changedFields
    }
}