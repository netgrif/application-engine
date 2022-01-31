package com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;

import java.util.List;

@Data
public abstract class PetriNetEventOutcome extends EventOutcome {

    private PetriNet net;

    protected PetriNetEventOutcome() {
    }

    protected PetriNetEventOutcome(PetriNet net) {
        this.net = net;
    }

    protected PetriNetEventOutcome(I18nString message, PetriNet net) {
        super(message);
        this.net = net;
    }

    protected PetriNetEventOutcome(I18nString message, List<EventOutcome> outcomes, PetriNet net) {
        super(message, outcomes);
        this.net = net;
    }

    public PetriNet getNet() {
        return net;
    }

    public void setNet(PetriNet net) {
        this.net = net;
    }
}
