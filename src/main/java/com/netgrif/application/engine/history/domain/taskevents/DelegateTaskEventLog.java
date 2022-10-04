package com.netgrif.application.engine.history.domain.taskevents;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "eventLogs")
@EqualsAndHashCode(callSuper = true)
public class DelegateTaskEventLog extends TaskEventLog {

    @Getter
    private String delegator;

    @Getter
    private String delegate;

    public DelegateTaskEventLog() {
        super();
    }

    public DelegateTaskEventLog(Task task, Case useCase, EventPhase eventPhase, IUser delegator, String delegate) {
        super(task, useCase, eventPhase, delegator.getStringId(), delegator.isImpersonating() ? delegator.getImpersonated().getStringId() : null);
        this.delegator = getUserId();
        this.delegate = delegate;
    }
}
