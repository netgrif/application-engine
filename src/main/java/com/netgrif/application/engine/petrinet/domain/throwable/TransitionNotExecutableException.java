package com.netgrif.application.engine.petrinet.domain.throwable;

public class TransitionNotExecutableException extends Exception {

    public TransitionNotExecutableException(String s) {
        super(s);
    }
}
