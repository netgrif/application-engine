package com.netgrif.application.engine.workflow.domain.eventoutcomes.response;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import org.springframework.hateoas.EntityModel;


public class EventOutcomeWithMessageResource {

    private EventOutcomeWithMessageResource() {
    }

    public static EntityModel<EventOutcomeWithMessage> successMessage(String successMsg, EventOutcome outcome) {
        return EntityModel.of(EventOutcomeWithMessage.withSuccessMessage(successMsg, outcome));
    }

    public static EntityModel<EventOutcomeWithMessage> errorMessage(String errorMsg) {
        return EntityModel.of(new EventOutcomeWithMessage(errorMsg));
    }

    public static EntityModel<EventOutcomeWithMessage> errorMessage(String errorMsg, EventOutcome outcome) {
        return EntityModel.of(EventOutcomeWithMessage.withErrorMessage(errorMsg, outcome));
    }
}
