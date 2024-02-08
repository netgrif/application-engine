package com.netgrif.application.engine.history.domain.caseevents;

import com.netgrif.application.engine.history.domain.petrinetevents.PetriNetEventLog;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class CaseEventLog extends PetriNetEventLog {

    protected String caseId;

    protected String caseTitle;

    private Map<String, Integer> activePlaces;

    private Map<String, Integer> consumedTokens;

    protected CaseEventLog() {
        super();
    }

    protected CaseEventLog(Case useCase, EventPhase eventPhase) {
        this(useCase.get_id(), useCase, eventPhase);
    }

    protected CaseEventLog(ObjectId triggerId, Case useCase, EventPhase eventPhase) {
        super(triggerId, eventPhase, useCase.getPetriNetObjectId());
        this.caseId = useCase.getStringId();
        this.caseTitle = useCase.getTitle();
        this.activePlaces = useCase.getActivePlaces();
        this.consumedTokens = useCase.getConsumedTokens();
    }
}
