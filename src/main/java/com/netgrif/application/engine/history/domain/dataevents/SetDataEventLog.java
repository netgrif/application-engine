package com.netgrif.application.engine.history.domain.dataevents;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.history.domain.taskevents.TaskEventLog;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import com.querydsl.core.annotations.QueryExclude;
import lombok.Getter;

@QueryExclude
public class SetDataEventLog extends TaskEventLog {

    @Getter
    private final DataSet changedFields;

    public SetDataEventLog(Task task, Case useCase, EventPhase eventPhase) {
        this(task, useCase, eventPhase, null);
    }

    public SetDataEventLog(Task task, Case useCase, EventPhase eventPhase, DataSet changedFields, IUser user) {
        super(task, useCase, eventPhase, user.isImpersonating() ? user.getImpersonated().getStringId() : null);
        this.changedFields = changedFields;
    }
}
