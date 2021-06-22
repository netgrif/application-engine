package com.netgrif.workflow.workflow.domain.eventoutcomes.response;

import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.workflow.workflow.web.responsebodies.ResponseMessage;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.LocalisedEventOutcome;
import lombok.Data;

import java.util.Map;

@Data
public class EventOutcomeWithMessage extends ResponseMessage {

    private LocalisedEventOutcome outcome;
    private Map<String, ChangedField> changedFields;

    public EventOutcomeWithMessage(String errorMessage){
        super();
        setError(errorMessage);
    }

    public EventOutcomeWithMessage(String successMessage, LocalisedEventOutcome outcome){
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
