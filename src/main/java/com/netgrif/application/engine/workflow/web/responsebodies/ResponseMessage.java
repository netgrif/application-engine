package com.netgrif.application.engine.workflow.web.responsebodies;

import lombok.Data;

@Data
public class ResponseMessage {

    private String code;

    private String success;

    private String error;

    private String data;

    public static ResponseMessage  createSuccessMessage(String msg, String code) {
        ResponseMessage resMsg = new ResponseMessage();
        resMsg.setSuccess(msg);
        resMsg.setCode(code);
        return resMsg;
    }

    public static ResponseMessage createSuccessMessageWithData(String msg, String data, String code) {
        ResponseMessage resMsg = new ResponseMessage();
        resMsg.setSuccess(msg);
        resMsg.setData(data);
        resMsg.setCode(code);
        return resMsg;
    }

    public static ResponseMessage createErrorMessage(String msg, String code) {
        ResponseMessage resMsg = new ResponseMessage();
        resMsg.setError(msg);
        resMsg.setCode(code);
        return resMsg;
    }


    public static ResponseMessage createErrorMessageWithData(String msg, String data, String code) {
        ResponseMessage resMsg = new ResponseMessage();
        resMsg.setError(msg);
        resMsg.setData(data);
        resMsg.setCode(code);
        return resMsg;
    }

}
