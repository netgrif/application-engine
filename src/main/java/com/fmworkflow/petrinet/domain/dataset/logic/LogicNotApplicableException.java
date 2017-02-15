package com.fmworkflow.petrinet.domain.dataset.logic;

public class LogicNotApplicableException extends RuntimeException {
    public LogicNotApplicableException(Exception e) {
        this.setStackTrace(e.getStackTrace());
    }
}
