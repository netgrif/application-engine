package com.netgrif.application.engine.petrinet.domain.dataset.logic

class CaseChangedFields {

    protected String caseId
    protected Map<String, ChangedField> changedFields = new HashMap<>()

    CaseChangedFields() {
    }

    CaseChangedFields(String caseId) {
        this.caseId = caseId
    }

    CaseChangedFields(String caseId, Map<String, ChangedField> changedFields) {
        this(caseId)
        this.changedFields = changedFields
    }

    void mergeChanges(Map<String, ChangedField> newChangedFields) {
        mergeChanges(this.changedFields, newChangedFields)
    }

    void mergeChanges(Map<String, ChangedField> changedFields, Map<String, ChangedField> newChangedFields) {
        newChangedFields.forEach({ fieldId, changedField ->
            if (changedFields.containsKey(fieldId)) {
                changedFields.get(fieldId).merge(changedField)

            } else {
                changedFields.put(fieldId, changedField)
            }
        })
    }

    String getCaseId() {
        return caseId
    }

    Map<String, ChangedField> getChangedFields() {
        return changedFields
    }
}
