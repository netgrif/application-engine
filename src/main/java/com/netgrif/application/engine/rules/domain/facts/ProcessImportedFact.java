package com.netgrif.application.engine.rules.domain.facts;

import com.netgrif.application.engine.workflow.domain.events.EventPhase;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessImportedFact extends ProcessFact {

    private EventPhase eventPhase;

    public ProcessImportedFact(String templateCaseId, EventPhase eventPhase) {
        super(templateCaseId);
        this.eventPhase = eventPhase;
    }
}
