package com.netgrif.application.engine.history.domain.taskevents;

import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
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
