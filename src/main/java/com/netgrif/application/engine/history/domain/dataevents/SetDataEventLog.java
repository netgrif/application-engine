package com.netgrif.application.engine.history.domain.dataevents;

import com.netgrif.application.engine.history.domain.taskevents.TaskEventLog;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.DataField;
import com.netgrif.application.engine.workflow.domain.Task;
import com.querydsl.core.annotations.QueryExclude;
import lombok.Getter;

import java.util.Map;

@QueryExclude
public class SetDataEventLog extends TaskEventLog {

    @Getter
    private final Map<String, DataField> changedFields;

    public SetDataEventLog(Task task, Case useCase, EventPhase eventPhase) {
        this(task, useCase, eventPhase, null);
    }

    public SetDataEventLog(Task task, Case useCase, EventPhase eventPhase, Map<String, DataField> changedFields) {
        super(task, useCase, eventPhase);
        this.changedFields = changedFields;
    }
}
