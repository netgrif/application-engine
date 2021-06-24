package com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetOutcome;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedImportPetriNetOutcome extends LocalisedEventOutcome {

    private PetriNetReference net;

    public LocalisedImportPetriNetOutcome(ImportPetriNetOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.net = new PetriNetReference(outcome.getNet(), locale);
    }
}
