package com.netgrif.workflow.workflow.domain.eventoutcomes.response;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReferenceWithMessage;
import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base.LocalisedEventOutcome;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class EventOutcomeWithMessageResource extends CollectionModel<EventOutcomeWithMessage> {

    public EventOutcomeWithMessageResource(EventOutcomeWithMessage content) {
        super(Collections.singleton(content), new ArrayList<Link>());
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
