package com.netgrif.workflow.workflow.domain;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.logic.TaskChangedFieldContainer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventOutcome {

    private String taskId;

    private I18nString message;

    private TaskChangedFieldContainer changedFields;

    private User assignee;

    private LocalDateTime startDate;
    
    private LocalDateTime finishDate;

    public void add(TaskChangedFieldContainer taskChangedFieldContainer) {
        this.changedFields.mergeChanges(taskChangedFieldContainer.getChangedFields());
    }

    public EventOutcome(String taskId) {
        this.changedFields = new TaskChangedFieldContainer();
    }

    public EventOutcome(String taskId, I18nString message) {
        this(taskId);
        this.message = message;
    }
}