package com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class PetriNetReferenceEventOutcome extends EventOutcome {

    private PetriNetReference net;

    public PetriNetReferenceEventOutcome(PetriNet net, Locale locale) {
        this.net = new PetriNetReference(net, locale);
    }
}
