package com.netgrif.application.engine.history.domain.taskevents;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.Getter;

public class DelegateTaskEventLog extends TaskEventLog {

    @Getter
    private String delegator;

    @Getter
    private String delegatee;

    public DelegateTaskEventLog(Task task, Case useCase, EventPhase eventPhase, IUser delegator, String delegatee) {
        super(task, useCase, eventPhase, delegator.getStringId(), delegator.isImpersonating() ? delegator.getImpersonated().getStringId() : null);
        this.delegator = getUserId();
        this.delegatee = delegatee;
    }
}
