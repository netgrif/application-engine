package com.netgrif.application.engine.workflow.web.responsebodies;


import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import java.util.ArrayList;

public class MessageResource extends EntityModel<ResponseMessage> {

    public MessageResource(ResponseMessage content) {
        super(content, new ArrayList<Link>());
    }

    public static MessageResource successMessage(String msg, String code) {
        return new MessageResource(ResponseMessage.createSuccessMessage(msg, code));
    }

    public static MessageResource successMessage(String msg, String data, String code) {
        return new MessageResource(ResponseMessage.createSuccessMessageWithData(msg, data, code));
    }

    public static MessageResource errorMessage(String msg, String code) {
        return new MessageResource(ResponseMessage.createErrorMessage(msg, code));
    }

    public static MessageResource errorMessage(String msg, String data, String code) {
        return new MessageResource(ResponseMessage.createErrorMessageWithData(msg, data, code));
    }

//    @Deprecated
//    public static MessageResource successMessage(String msg) {
//        return new MessageResource(ResponseMessage.createSuccessMessage(msg, ResponseMessageCode.success));
//    }

//    @Deprecated
//    public static MessageResource errorMessage(String msg) {
//        return new MessageResource(ResponseMessage.createSuccessMessage(msg, ResponseMessageCode.error));
//    }
//
//    @Deprecated
//    public static MessageResource successMessage(String msg, String data) {
//        return new MessageResource(ResponseMessage.createSuccessMessageWithData(msg, data, ResponseMessageCode.success));
//    }
//
//    @Deprecated
//    public static MessageResource errorMessage(String msg, String data) {
//        return new MessageResource(ResponseMessage.createErrorMessageWithData(msg, data, ResponseMessageCode.error));
//    }

}
