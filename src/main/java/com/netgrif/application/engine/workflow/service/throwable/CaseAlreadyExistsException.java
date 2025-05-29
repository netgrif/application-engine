package com.netgrif.application.engine.workflow.service.throwable;

public class CaseAlreadyExistsException extends RuntimeException {

    public CaseAlreadyExistsException(String message) {
        super(message);
    }
}
