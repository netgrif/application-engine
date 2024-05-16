package com.netgrif.application.engine.integration.plugins.outcomes;

import com.netgrif.application.engine.workflow.domain.Case;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetOrCreateOutcome {
    private final Case subjectCase;
    private final boolean isNew;
    private Object additionalData;

    public GetOrCreateOutcome(Case subjectCase, boolean isNew) {
        this.subjectCase = subjectCase;
        this.isNew = isNew;
    }
}
