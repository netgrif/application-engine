package com.netgrif.application.engine.objects.petrinet.domain.dataset.logic;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class CaseChangedFields implements Serializable {

    @Serial
    private static final long serialVersionUID = 2299918326436821185L;

    protected String caseId;

    protected Map<String, ChangedField> changedFields = new HashMap<String, ChangedField>();

    public CaseChangedFields() {
    }

    public CaseChangedFields(String caseId) {
        this.caseId = caseId;
    }

    public CaseChangedFields(String caseId, Map<String, ChangedField> changedFields) {
        this(caseId);
        this.changedFields = changedFields;
    }

    public void mergeChanges(Map<String, ChangedField> newChangedFields) {
        mergeChanges(this.changedFields, newChangedFields);
    }

    public void mergeChanges(final Map<String, ChangedField> changedFields, Map<String, ChangedField> newChangedFields) {
        newChangedFields.forEach((k, v) -> {
            if (changedFields.containsKey(k)) {
                changedFields.get(k).merge(v);
            } else {
                changedFields.put(k, v);
            }
        });
    }

}
