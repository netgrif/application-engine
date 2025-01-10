package com.netgrif.application.engine.workflow.domain.throwable;

public class TransitionNotExecutableException extends Exception {

    public TransitionNotExecutableException(String s) {
        super(s);
    }
}
