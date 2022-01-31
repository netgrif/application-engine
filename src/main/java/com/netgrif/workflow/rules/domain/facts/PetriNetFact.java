package com.netgrif.workflow.rules.domain.facts;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class PetriNetFact extends Fact {

    private String netId;

    public PetriNetFact(String netId) {
        super();
        this.netId = netId;
    }
}
