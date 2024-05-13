package com.netgrif.application.engine.integration.plugins.outcomes;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class CreateOrUpdateOutcome {
    private final Set<String> createdCaseIds;
    private final Set<String> subjectCaseIds;

    public CreateOrUpdateOutcome() {
        createdCaseIds = new HashSet<>();
        subjectCaseIds = new HashSet<>();
    }

    public boolean addCreatedAndSubjectCaseId(String caseId) {
        if (caseId == null) {
            return false;
        } else {
            boolean isCreatedChanged = createdCaseIds.add(caseId);
            return subjectCaseIds.add(caseId) || isCreatedChanged; // do not change expression order
        }
    }

    public boolean addSubjectCaseId(String caseId) {
        if (caseId == null) {
            return false;
        } else {
            return subjectCaseIds.add(caseId);
        }
    }

    public boolean addAllCreatedCaseId(Set<String> caseIds) {
        return createdCaseIds.addAll(caseIds);
    }
}
