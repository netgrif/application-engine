package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base;

import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.PetriNetEventOutcome;

import java.util.List;
import java.util.Locale;

public abstract class LocalisedPetriNetEventOutcome extends LocalisedEventOutcome{

    private PetriNetReference net;

    protected LocalisedPetriNetEventOutcome(PetriNetEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.net = new PetriNetReference(outcome.getNet(), locale);
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
