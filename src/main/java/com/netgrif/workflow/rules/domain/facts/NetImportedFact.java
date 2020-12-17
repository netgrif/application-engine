package com.netgrif.workflow.rules.domain.facts;

import com.netgrif.workflow.petrinet.domain.events.EventPhase;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NetImportedFact extends PetriNetFact {

    private EventPhase eventPhase;

    public NetImportedFact(String netId, EventPhase eventPhase) {
        super(netId);
        this.eventPhase = eventPhase;
    }
}
