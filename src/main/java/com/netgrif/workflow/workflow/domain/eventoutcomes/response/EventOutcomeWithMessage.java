package com.netgrif.workflow.workflow.domain.eventoutcomes.response;

import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.workflow.workflow.web.responsebodies.ResponseMessage;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base.LocalisedEventOutcome;

import java.util.HashMap;
import java.util.Map;

public class EventOutcomeWithMessage extends ResponseMessage {

    private LocalisedEventOutcome outcome;
    private Map<String, ChangedField> changedFields = new HashMap<>();

    public EventOutcomeWithMessage(String errorMessage){
        super();
        setError(errorMessage);
    }

    public EventOutcomeWithMessage(String successMessage, LocalisedEventOutcome outcome){
        super();
        this.outcome = outcome;
        setSuccess(successMessage);
    }

    public EventOutcomeWithMessage(String errorMessage, Map<String, ChangedField> changedFields ){
        super();
        setError(errorMessage);
        this.changedFields.putAll(changedFields);
    }

}
