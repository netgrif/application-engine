package com.netgrif.application.engine.workflow.domain.throwable;

public class IllegalTaskStateException extends IllegalStateException {

    public IllegalTaskStateException(String message) {
        super(message);
    }
}
