package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base;

import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;

import java.util.List;
import java.util.Locale;

public abstract class LocalisedPetriNetEventOutcome extends LocalisedEventOutcome{

    private PetriNetReference net;

    protected LocalisedPetriNetEventOutcome(EventOutcome outcome, Locale locale, PetriNetReference net) {
        super(outcome, locale);
        this.net = net;
    }

    protected LocalisedPetriNetEventOutcome(String message, List<LocalisedEventOutcome> outcomes, PetriNetReference net) {
        super(message, outcomes);
        this.net = net;
    }

    public PetriNetReference getNet() {
        return net;
    }

    public void setNet(PetriNetReference net) {
        this.net = net;
    }
}
