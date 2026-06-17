package com.netgrif.application.engine.petrinet.domain.roles;

public class LogicNotApplicableException extends RuntimeException {
    public LogicNotApplicableException(Exception e) {
        this.setStackTrace(e.getStackTrace());
    }
}
