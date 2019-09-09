package com.netgrif.workflow.workflow.web.responsebodies;


public class ResponseMessage {

    private String success;
    private String error;
    private Object data;

    public static ResponseMessage createSuccessMessage(String msg){
        ResponseMessage resMsg = new ResponseMessage();
        resMsg.setSuccess(msg);
        return resMsg;
    }

    public static ResponseMessage createSuccessMessageWithData(String msg, Object data){
        ResponseMessage resMsg = new ResponseMessage();
        resMsg.setSuccess(msg);
        resMsg.setData(data);
        return resMsg;
    }

    public static ResponseMessage createErrorMessage(String msg){
        ResponseMessage resMsg = new ResponseMessage();
        resMsg.setError(msg);
        return resMsg;
    }


    public static ResponseMessage createErrorMessageWithData(String msg, Object data){
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
