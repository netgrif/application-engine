package com.netgrif.application.engine.workflow.web.responsebodies;


import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import java.util.ArrayList;

public class MessageResource extends EntityModel<ResponseMessage> {

    public MessageResource(ResponseMessage content) {
        super(content, new ArrayList<Link>());
    }

    public static MessageResource successMessage(String msg) {
        return new MessageResource(ResponseMessage.createSuccessMessage(msg));
    }

    public static MessageResource successMessage(String msg, String data) {
        return new MessageResource(ResponseMessage.createSuccessMessageWithData(msg, data));
    }

    public static MessageResource errorMessage(String msg) {
        return new MessageResource(ResponseMessage.createErrorMessage(msg));
    }

    public static MessageResource errorMessage(String msg, String data) {
        return new MessageResource(ResponseMessage.createErrorMessageWithData(msg, data));
    }
}
