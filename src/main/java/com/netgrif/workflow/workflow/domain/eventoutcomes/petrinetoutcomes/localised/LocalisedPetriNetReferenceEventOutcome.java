package com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes.localised;

import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.workflow.domain.eventoutcomes.LocalisedEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedPetriNetReferenceEventOutcome extends LocalisedEventOutcome {

    private PetriNetReference net;

    public LocalisedPetriNetReferenceEventOutcome(ImportPetriNetOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.net = new PetriNetReference(outcome.getNet(), locale);
    }
}
