package com.netgrif.workflow.workflow.domain.eventoutcomes.response;

import com.netgrif.workflow.workflow.web.responsebodies.eventoutcomes.base.LocalisedEventOutcome;
import org.springframework.hateoas.EntityModel;


public class EventOutcomeWithMessageResource {

    private EventOutcomeWithMessageResource(){
    }

    public static EntityModel<EventOutcomeWithMessage> successMessage(String successMsg, LocalisedEventOutcome outcome){
        return  EntityModel.of(EventOutcomeWithMessage.withSuccessMessage(successMsg,outcome));
    }

    public static EntityModel<EventOutcomeWithMessage> errorMessage(String errorMsg){
        return EntityModel.of(new EventOutcomeWithMessage(errorMsg));
    }

    public static EntityModel<EventOutcomeWithMessage> errorMessage(String errorMsg, LocalisedEventOutcome outcome){
        return EntityModel.of(EventOutcomeWithMessage.withErrorMessage(errorMsg, outcome));
    }
}
