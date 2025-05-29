package com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.petrinetoutcomes;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.EventOutcome;
import lombok.Data;

import java.util.List;

@Data
public abstract class PetriNetEventOutcome extends EventOutcome {

    private Process process;

    protected PetriNetEventOutcome() {
    }

    protected PetriNetEventOutcome(Process process) {
        this.process = process;
    }

    protected PetriNetEventOutcome(I18nString message, Process process) {
        super(message);
        this.process = process;
    }

    protected PetriNetEventOutcome(I18nString message, List<EventOutcome> outcomes, Process process) {
        super(message, outcomes);
        this.process = process;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }
}
