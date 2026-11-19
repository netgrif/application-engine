package com.netgrif.application.engine.workflow.service;

public class TimeTriggerRetryableException extends RuntimeException {

    public TimeTriggerRetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
