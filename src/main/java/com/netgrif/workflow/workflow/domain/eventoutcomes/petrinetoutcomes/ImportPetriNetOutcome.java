package com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.LocalisedEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes.localised.LocalisedPetriNetReferenceEventOutcome;
import lombok.Data;

import java.util.List;
import java.util.Locale;

@Data
public class ImportPetriNetOutcome extends EventOutcome {

    private PetriNet net;

    public ImportPetriNetOutcome() {
    }

    public ImportPetriNetOutcome(I18nString message, List<EventOutcome> outcomes, PetriNet net) {
        super(message, outcomes);
        this.net = net;
    }

    @Override
    public LocalisedPetriNetReferenceEventOutcome transformToLocalisedEventOutcome(Locale locale) {
        return new LocalisedPetriNetReferenceEventOutcome(this, locale);
    }
}
