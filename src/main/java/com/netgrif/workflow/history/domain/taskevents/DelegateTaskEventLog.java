package com.netgrif.workflow.history.domain.taskevents;

import com.netgrif.workflow.petrinet.domain.events.EventPhase;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.Getter;

public class DelegateTaskEventLog extends TaskEventLog{

    @Getter
    private String delegator;

    @Getter
    private String delegatee;

    public DelegateTaskEventLog(Task task, Case useCase, EventPhase eventPhase, String delegator, String delegatee) {
        super(task, useCase, eventPhase);
        this.delegator = delegator;
        this.delegatee = delegatee;
    }
}
