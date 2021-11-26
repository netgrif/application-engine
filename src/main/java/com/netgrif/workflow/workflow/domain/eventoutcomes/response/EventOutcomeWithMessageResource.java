package com.netgrif.workflow.workflow.domain.eventoutcomes.response;

import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base.LocalisedEventOutcome;
import org.springframework.hateoas.Resource;

import java.util.ArrayList;

public class EventOutcomeWithMessageResource extends Resource<EventOutcomeWithMessage> {

    public EventOutcomeWithMessageResource(EventOutcomeWithMessage content) {
        super(content, new ArrayList<>());
    }

    public static EventOutcomeWithMessageResource successMessage(String successMsg, LocalisedEventOutcome outcome){
        return new EventOutcomeWithMessageResource(EventOutcomeWithMessage.withSuccessMessage(successMsg,outcome));
    }

    public static EventOutcomeWithMessageResource errorMessage(String errorMsg){
        return new EventOutcomeWithMessageResource(new EventOutcomeWithMessage(errorMsg));
    }

    public static EventOutcomeWithMessageResource errorMessage(String errorMsg, LocalisedEventOutcome outcome){
        return new EventOutcomeWithMessageResource(EventOutcomeWithMessage.withErrorMessage(errorMsg, outcome));
    }
}
