package com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes;

import com.netgrif.application.engine.workflow.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;

import java.util.List;

@Data
public abstract class PetriNetEventOutcome extends EventOutcome {

    private Process net;

    protected PetriNetEventOutcome() {
    }

    protected PetriNetEventOutcome(Process net) {
        this.net = net;
    }

    protected PetriNetEventOutcome(I18nString message, Process net) {
        super(message);
        this.net = net;
    }

    protected PetriNetEventOutcome(I18nString message, List<EventOutcome> outcomes, Process net) {
        super(message, outcomes);
        this.net = net;
    }

    public Process getNet() {
        return net;
    }

    public void setNet(Process net) {
        this.net = net;
    }
}
