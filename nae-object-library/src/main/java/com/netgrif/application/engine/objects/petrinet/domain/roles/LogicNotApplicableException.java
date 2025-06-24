package com.netgrif.application.engine.objects.petrinet.domain.roles;

public class LogicNotApplicableException extends RuntimeException {
    public LogicNotApplicableException(Exception e) {
        this.setStackTrace(e.getStackTrace());
    }
}
