package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base;

import com.netgrif.application.engine.objects.dto.response.petrinet.PetriNetReferenceDto;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.petrinetoutcomes.PetriNetEventOutcome;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Locale;

public abstract class LocalisedPetriNetEventOutcome extends LocalisedEventOutcome {

    @Getter
    @Setter
    private PetriNetReferenceDto net;

    protected LocalisedPetriNetEventOutcome(PetriNetEventOutcome outcome, Locale locale) {
        super(outcome, locale);
        this.net = PetriNetReferenceDto.fromPetriNet(outcome.getNet(), locale);
    }

    protected LocalisedPetriNetEventOutcome(String message, List<LocalisedEventOutcome> outcomes, PetriNetReferenceDto net) {
        super(message, outcomes);
        this.net = net;
    }
}
