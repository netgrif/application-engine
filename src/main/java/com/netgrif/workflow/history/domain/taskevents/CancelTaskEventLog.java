package com.netgrif.workflow.history.domain.taskevents;

import com.netgrif.workflow.petrinet.domain.events.EventPhase;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Getter;

public class CancelTaskEventLog extends TaskEventLog {

    @Getter
    private Long userId;

    public CancelTaskEventLog(Task task, Case useCase, EventPhase eventPhase, Long userId) {
        super(task, useCase, eventPhase);
        this.userId = userId;
    }
}
