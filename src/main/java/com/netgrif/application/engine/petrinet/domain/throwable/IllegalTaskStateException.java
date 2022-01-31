package com.netgrif.application.engine.petrinet.domain.throwable;

public class IllegalTaskStateException extends IllegalStateException {

    public IllegalTaskStateException(String message) {
        super(message);
    }
}
