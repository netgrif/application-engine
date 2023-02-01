package com.netgrif.application.engine.history.domain.dataevents;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.history.domain.taskevents.TaskEventLog;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.querydsl.core.annotations.QueryExclude;
import lombok.Getter;

import java.util.Map;

@QueryExclude
public class SetDataEventLog extends TaskEventLog {

    @Getter
    private Map<String, ChangedField> changedFields;

    public SetDataEventLog(Task task, Case useCase, EventPhase eventPhase, Map<String, ChangedField> changedFields, IUser user) {
        super(task, useCase, eventPhase, user.getStringId(), user.isImpersonating() ? user.getImpersonated().getStringId() : null);
        this.changedFields = changedFields;
    }
}
