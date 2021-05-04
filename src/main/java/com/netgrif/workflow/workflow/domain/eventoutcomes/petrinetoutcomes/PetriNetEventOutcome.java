package com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;

import java.util.List;

@Data
public abstract class PetriNetEventOutcome extends EventOutcome {

    private PetriNet net;

    public PetriNetEventOutcome(I18nString message, List<EventOutcome> outcomes, PetriNet net) {
        super(message, outcomes);
        this.net = net;
    }

    public PetriNetEventOutcome() {
    }
}
