package com.netgrif.application.engine.history.domain.caseevents;

import com.netgrif.application.engine.history.domain.processevents.ProcessEventLog;
import com.netgrif.application.engine.workflow.domain.I18nString;
import com.netgrif.application.engine.workflow.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class CaseEventLog extends ProcessEventLog {

    protected String caseId;

    protected I18nString caseTitle;

    private Map<String, Integer> activePlaces;

    private Map<String, Integer> consumedTokens;

    protected CaseEventLog() {
        super();
    }

    protected CaseEventLog(Case useCase, EventPhase eventPhase) {
        this(useCase.getId(), useCase, eventPhase);
    }

    protected CaseEventLog(ObjectId triggerId, Case useCase, EventPhase eventPhase) {
        super(triggerId, eventPhase, useCase.getTemplateCaseId());
        this.caseId = useCase.getStringId();
        this.caseTitle = useCase.getTitle();
        this.activePlaces = useCase.getActivePlaces();
        this.consumedTokens = useCase.getConsumedTokens();
    }
}
