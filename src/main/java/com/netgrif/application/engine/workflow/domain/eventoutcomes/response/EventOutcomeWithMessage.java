package com.netgrif.application.engine.workflow.domain.eventoutcomes.response;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.ResponseMessage;
import lombok.Data;

@Data
public class EventOutcomeWithMessage extends ResponseMessage {

    private EventOutcome outcome;

    public EventOutcomeWithMessage(EventOutcome outcome) {
        this.outcome = outcome;
    }
    public EventOutcomeWithMessage(String errorMsg) {
        setError(errorMsg);
    }

    public static EventOutcomeWithMessage withSuccessMessage(String successMessage, EventOutcome outcome){
        EventOutcomeWithMessage outcomeWithMessage = new EventOutcomeWithMessage(outcome);
        outcomeWithMessage.setSuccess(successMessage);
        return outcomeWithMessage;
    }

    public static EventOutcomeWithMessage withErrorMessage(String errorMessage, EventOutcome outcome){
        EventOutcomeWithMessage outcomeWithMessage = new EventOutcomeWithMessage(outcome);
        outcomeWithMessage.setError(errorMessage);
        return outcomeWithMessage;
    }
}
