package com.netgrif.workflow.history.domain.dataevents;

import com.netgrif.workflow.history.domain.taskevents.TaskEventLog;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.workflow.petrinet.domain.events.EventPhase;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.querydsl.core.annotations.QueryExclude;
import lombok.Getter;

import java.util.Map;

@QueryExclude
public class SetDataEventLog extends TaskEventLog {

    @Getter
    private Map<String, ChangedField> changedFields;

    public SetDataEventLog(Task task, Case useCase, EventPhase eventPhase, Map<String, ChangedField> changedFields) {
        super(task, useCase, eventPhase);
        this.changedFields = changedFields;
    }
}
