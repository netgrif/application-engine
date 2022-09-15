package com.netgrif.application.engine.history.domain.taskevents;

import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.Getter;

public class FinishTaskEventLog extends TaskEventLog{

    @Getter
    private String userId;

    public FinishTaskEventLog() {
        super();
    }

    public FinishTaskEventLog(Task task, Case useCase, EventPhase eventPhase, String userId) {
        super(task, useCase, eventPhase);
        this.userId = userId;
    }
}
