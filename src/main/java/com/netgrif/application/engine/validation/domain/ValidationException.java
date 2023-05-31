package com.netgrif.application.engine.validation.domain;

public class ValidationException extends Exception {

    public ValidationException() {
    }

    public ValidationException(String message) {
        super(message);
    }

}
