package com.netgrif.application.engine.petrinet.domain.dataset.logic

import com.fasterxml.jackson.annotation.JsonInclude
import com.netgrif.application.engine.petrinet.domain.ChangedField

@JsonInclude(JsonInclude.Include.NON_NULL)
class ChangedFieldContainer implements Serializable {

    private static final long serialVersionUID = 2299918326411121185L;

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