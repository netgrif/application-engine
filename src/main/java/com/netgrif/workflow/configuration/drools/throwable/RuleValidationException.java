package com.netgrif.workflow.configuration.drools.throwable;

public class RuleValidationException extends Exception {

    public RuleValidationException(Exception e) {
        super("Validation failed", e);
    }
}
