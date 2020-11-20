package com.netgrif.workflow.workflow.domain;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class EventOutcome {

    private String taskId;

    private I18nString message;

    private Map<String, Map<String, ChangedField>> changedFields;

    private User assignee;

    private LocalDateTime startDate;
    
    private LocalDateTime finishDate;

    public void add(Map<String, Map<String, ChangedField>> changedFields) {
        this.changedFields.putAll(changedFields);
    }

    public EventOutcome() {
        this.changedFields = new HashMap<>();
    }

    public EventOutcome(I18nString message) {
        this();
        this.message = message;
    }
}