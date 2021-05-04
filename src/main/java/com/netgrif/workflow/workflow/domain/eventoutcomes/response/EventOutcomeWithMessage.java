package com.netgrif.workflow.workflow.domain.eventoutcomes.response;

import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.ResponseMessage;
import lombok.Data;

import java.util.Map;

@Data
public class EventOutcomeWithMessage extends ResponseMessage {

    private EventOutcome outcome;
    private Map<String, ChangedField> changedFields;

    public EventOutcomeWithMessage(String errorMessage){
        super();
        setError(errorMessage);
    }

    public EventOutcomeWithMessage(String successMessage, EventOutcome outcome){
        super();
        setOutcome(outcome);
        setSuccess(successMessage);
    }

    public EventOutcomeWithMessage(String errorMessage, Map<String, ChangedField> changedFields ){
        super();
        setError(errorMessage);
        this.changedFields.putAll(changedFields);
    }

}
