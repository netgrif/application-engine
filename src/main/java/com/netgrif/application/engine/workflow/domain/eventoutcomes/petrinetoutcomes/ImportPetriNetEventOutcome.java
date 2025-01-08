package com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes;

import com.netgrif.application.engine.workflow.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;

import java.util.List;

@Data
public class ImportPetriNetEventOutcome extends PetriNetEventOutcome {

    public ImportPetriNetEventOutcome() {
    }

    public ImportPetriNetEventOutcome(Process net) {
        super(net);
    }

    public ImportPetriNetEventOutcome(I18nString message, Process net) {
        super(message, net);
    }

    public ImportPetriNetEventOutcome(I18nString message, List<EventOutcome> outcomes, Process net) {
        super(message, outcomes, net);
    }
}
