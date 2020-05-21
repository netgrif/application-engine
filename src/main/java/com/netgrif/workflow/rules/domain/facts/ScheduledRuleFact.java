package com.netgrif.workflow.rules.domain.facts;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ScheduledRuleFact extends CaseFact {

    private String ruleIdentifier;

    public ScheduledRuleFact(String caseId, String ruleIdentifier) {
        super(caseId);
        this.ruleIdentifier = ruleIdentifier;
    }
}


