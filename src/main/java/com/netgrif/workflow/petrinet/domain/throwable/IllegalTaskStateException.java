package com.netgrif.workflow.petrinet.domain.throwable;

public class IllegalTaskStateException extends IllegalStateException {

    public IllegalTaskStateException(String message) {
        super(message);
    }
}
