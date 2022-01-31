package com.netgrif.application.engine.workflow.domain.eventoutcomes.response;

import com.netgrif.application.engine.workflow.web.responsebodies.ResponseMessage;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedEventOutcome;
import lombok.Data;

@Data
public class EventOutcomeWithMessage extends ResponseMessage {

    private LocalisedEventOutcome outcome;

    public EventOutcomeWithMessage(LocalisedEventOutcome outcome) {
        this.outcome = outcome;
    }
    public EventOutcomeWithMessage(String errorMsg) {
        setError(errorMsg);
    }

    public static EventOutcomeWithMessage withSuccessMessage(String successMessage, LocalisedEventOutcome outcome){
        EventOutcomeWithMessage outcomeWithMessage = new EventOutcomeWithMessage(outcome);
        outcomeWithMessage.setSuccess(successMessage);
        return outcomeWithMessage;
    }

    public static EventOutcomeWithMessage withErrorMessage(String errorMessage, LocalisedEventOutcome outcome){
        EventOutcomeWithMessage outcomeWithMessage = new EventOutcomeWithMessage(outcome);
        outcomeWithMessage.setError(errorMessage);
        return outcomeWithMessage;
    }
}
