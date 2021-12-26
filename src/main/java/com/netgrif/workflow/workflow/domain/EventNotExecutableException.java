package com.netgrif.workflow.workflow.domain;

public class EventNotExecutableException extends RuntimeException {

    public EventNotExecutableException(String message, Throwable cause) {
        super(message, cause);
    }
}
