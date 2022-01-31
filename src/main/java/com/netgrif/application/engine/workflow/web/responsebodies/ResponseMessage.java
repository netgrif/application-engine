package com.netgrif.application.engine.workflow.web.responsebodies;


public class ResponseMessage {

    private String success;
    private String error;
    private String data;

    public static ResponseMessage createSuccessMessage(String msg) {
        ResponseMessage resMsg = new ResponseMessage();
        resMsg.setSuccess(msg);
        return resMsg;
    }

    public static ResponseMessage createSuccessMessageWithData(String msg, String data) {
        ResponseMessage resMsg = new ResponseMessage();
        resMsg.setSuccess(msg);
        resMsg.setData(data);
        return resMsg;
    }

    public static ResponseMessage createErrorMessage(String msg) {
        ResponseMessage resMsg = new ResponseMessage();
        resMsg.setError(msg);
        return resMsg;
    }


    public static ResponseMessage createErrorMessageWithData(String msg, String data) {
        ResponseMessage resMsg = new ResponseMessage();
        resMsg.setError(msg);
        resMsg.setData(data);
        return resMsg;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
