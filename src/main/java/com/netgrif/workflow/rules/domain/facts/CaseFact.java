package com.netgrif.workflow.rules.domain.facts;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class CaseFact extends Fact {

    private String caseId;

    public CaseFact(String caseId) {
        super();
        this.caseId = caseId;
    }
}
