package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class EventOutcome {

    private I18nString message;

    private Map<String, ChangedField> changedFields;

    public void add(Map<String, ChangedField> changedFields) {
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