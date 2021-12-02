package com.netgrif.workflow.history.domain.taskevents;

import com.netgrif.workflow.petrinet.domain.events.EventPhase;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Getter;

public class FinishTaskEventLog extends TaskEventLog{

    @Getter
    private String userId;

    public FinishTaskEventLog(Task task, Case useCase, EventPhase eventPhase, String userId) {
        super(task, useCase, eventPhase);
        this.userId = userId;
    }
}
