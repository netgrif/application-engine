package com.netgrif.application.engine.rules.domain.facts;

import com.netgrif.application.engine.workflow.domain.events.EventPhase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@EqualsAndHashCode(callSuper = true)
public class CaseCreatedFact extends CaseFact {

    private EventPhase eventPhase;

    public CaseCreatedFact(String caseId, EventPhase eventPhase) {
        super(caseId);
        this.eventPhase = eventPhase;
    }

}

