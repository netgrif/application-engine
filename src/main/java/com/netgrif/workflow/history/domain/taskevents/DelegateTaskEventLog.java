package com.netgrif.workflow.history.domain.taskevents;

import com.netgrif.workflow.petrinet.domain.events.EventPhase;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Getter;

public class DelegateTaskEventLog extends TaskEventLog{

    @Getter
    private Long delegator;

    @Getter
    private Long delegatee;

    public DelegateTaskEventLog(Task task, Case useCase, EventPhase eventPhase, Long delegator, Long delegatee) {
        super(task, useCase, eventPhase);
        this.delegator = delegator;
        this.delegatee = delegatee;
    }
}
