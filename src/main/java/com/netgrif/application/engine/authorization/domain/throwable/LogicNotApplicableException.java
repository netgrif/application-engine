package com.netgrif.application.engine.authorization.domain.throwable;

public class LogicNotApplicableException extends RuntimeException {
    public LogicNotApplicableException(Exception e) {
        this.setStackTrace(e.getStackTrace());
    }
}
