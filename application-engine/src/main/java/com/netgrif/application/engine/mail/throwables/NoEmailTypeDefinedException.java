package com.netgrif.application.engine.mail.throwables;

public class NoEmailTypeDefinedException extends RuntimeException {

    public NoEmailTypeDefinedException(String errorMessage) {
        super(errorMessage);
    }
}
