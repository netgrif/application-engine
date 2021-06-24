package com.netgrif.workflow.workflow.domain.eventoutcomes.response;

import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.LocalisedEventOutcome;
import org.springframework.hateoas.Resource;

import java.util.ArrayList;
import java.util.Map;

public class EventOutcomeWithMessageResource extends Resource<EventOutcomeWithMessage> {

    public EventOutcomeWithMessageResource(EventOutcomeWithMessage content) {
        super(content, new ArrayList<>());
    }

    public static EventOutcomeWithMessageResource successMessage(String successMsg, LocalisedEventOutcome outcome){
        return new EventOutcomeWithMessageResource(new EventOutcomeWithMessage(successMsg,outcome));
    }

    public static EventOutcomeWithMessageResource errorMessage(String errorMsg){
        return new EventOutcomeWithMessageResource(new EventOutcomeWithMessage(errorMsg));
    }

    public static EventOutcomeWithMessageResource errorMessage(String errorMsg, Map<String, ChangedField> changedFields){
        return new EventOutcomeWithMessageResource(new EventOutcomeWithMessage(errorMsg, changedFields));
    }
}
