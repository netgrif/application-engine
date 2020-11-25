package com.netgrif.workflow.workflow.domain;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldsTree;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventOutcome {

    private String taskId;

    private String caseId;

    private String transitionId;

    private I18nString message;

    private ChangedFieldsTree changedFields;

    private User assignee;

    private LocalDateTime startDate;
    
    private LocalDateTime finishDate;

    public void add(ChangedFieldsTree changedFieldsTree) {
        this.changedFields.mergeChangedFields(changedFieldsTree);
    }

    public EventOutcome(String taskId, String transitionId, String caseId) {
        this.taskId = taskId;
        this.caseId = caseId;
        this.transitionId = transitionId;
        this.changedFields = ChangedFieldsTree.createNew(caseId, taskId, transitionId);
    }

    public EventOutcome(String taskId, String transitionId, String caseId, I18nString message) {
        this(taskId, transitionId, caseId);
        this.message = message;
    }
}