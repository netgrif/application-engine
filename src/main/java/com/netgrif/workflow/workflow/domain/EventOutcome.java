package com.netgrif.workflow.workflow.domain;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldsTree;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class EventOutcome {

    private String taskId;

    private I18nString message;

    private ChangedFieldsTree changedFields;

    private User assignee;

    private LocalDateTime startDate;
    
    private LocalDateTime finishDate;

    public void add(ChangedFieldsTree changedFields) {
        this.changedFields.mergeChangesOnTaskTree(changedFields);
    }

    public EventOutcome(String taskId) {
        this.changedFields = ChangedFieldsTree.createNew(taskId);
    }

    public EventOutcome(String taskId, I18nString message) {
        this(taskId);
        this.message = message;
    }
}