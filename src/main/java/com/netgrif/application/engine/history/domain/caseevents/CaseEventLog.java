package com.netgrif.application.engine.history.domain.caseevents;

import com.netgrif.application.engine.history.domain.petrinetevents.PetriNetEventLog;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;

@EqualsAndHashCode(callSuper = true)
public abstract class CaseEventLog extends PetriNetEventLog {

    protected String caseId;

    protected String caseTitle;

    protected CaseEventLog() {
        super();
    }

    protected CaseEventLog(ObjectId triggerId, Case useCase, EventPhase eventPhase) {
        super(triggerId, eventPhase, useCase.getPetriNetObjectId());
        this.caseId = useCase.getStringId();
        this.caseTitle = useCase.getTitle();
    }

    protected CaseEventLog(Case useCase, EventPhase eventPhase) {
        this(useCase.get_id(), useCase, eventPhase);
        this.caseId = useCase.getStringId();
        this.caseTitle = useCase.getTitle();
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getCaseTitle() {
        return caseTitle;
    }

    public void setCaseTitle(String caseTitle) {
        this.caseTitle = caseTitle;
    }
}
