package com.netgrif.workflow.history.domain.caseevents;

import com.netgrif.workflow.history.domain.petrinetevents.PetriNetEventLog;
import com.netgrif.workflow.petrinet.domain.events.EventPhase;
import com.netgrif.workflow.workflow.domain.Case;
import org.bson.types.ObjectId;

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
        super(useCase.get_id(), eventPhase, useCase.getPetriNetObjectId());
        this.caseId = useCase.getStringId();
        this.caseTitle = useCase.getTitle();
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public void setCaseTitle(String caseTitle) {
        this.caseTitle = caseTitle;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getCaseTitle() {
        return caseTitle;
    }
}