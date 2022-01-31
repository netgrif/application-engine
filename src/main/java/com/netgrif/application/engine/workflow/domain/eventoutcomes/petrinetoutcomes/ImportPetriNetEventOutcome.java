package com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;

import java.util.List;

@Data
public class ImportPetriNetEventOutcome extends PetriNetEventOutcome {

    public ImportPetriNetEventOutcome() {
    }

    public ImportPetriNetEventOutcome(PetriNet net) {
        super(net);
    }

    public ImportPetriNetEventOutcome(I18nString message, PetriNet net) {
        super(message, net);
    }

    public ImportPetriNetEventOutcome(I18nString message, List<EventOutcome> outcomes, PetriNet net) {
        super(message, outcomes, net);
    }
}
