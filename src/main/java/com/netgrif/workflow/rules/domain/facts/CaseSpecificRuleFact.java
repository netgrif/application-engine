package com.netgrif.workflow.rules.domain.facts;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class CaseSpecificRuleFact extends RuleFact {

    private String caseId;

    public CaseSpecificRuleFact(String caseId) {
        super();
        this.caseId = caseId;
    }
}
