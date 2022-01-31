package com.netgrif.workflow.rules.domain.facts;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ScheduledRuleFact extends Fact {

    private String instanceId;
    private String ruleIdentifier;

    public ScheduledRuleFact(String instanceId, String ruleIdentifier) {
        super();
        this.instanceId = instanceId;
        this.ruleIdentifier = ruleIdentifier;
    }
}


