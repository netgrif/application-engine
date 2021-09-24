package com.netgrif.workflow.history.domain.caseevents;

import com.netgrif.workflow.petrinet.domain.events.EventPhase;
import com.netgrif.workflow.workflow.domain.Case;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "eventLog")
public class DeleteCaseEventLog extends CaseEventLog {

    public DeleteCaseEventLog(Case useCase, EventPhase eventPhase) {
        super(useCase, eventPhase);
    }
}