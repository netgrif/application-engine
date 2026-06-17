package com.netgrif.application.engine.workflow.domain;

public class EventNotExecutableException extends RuntimeException {

    public EventNotExecutableException(String message, Throwable cause) {
        super(message, cause);
    }
}
