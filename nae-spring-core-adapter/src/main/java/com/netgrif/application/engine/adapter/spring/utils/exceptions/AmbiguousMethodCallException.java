package com.netgrif.application.engine.adapter.spring.utils.exceptions;

public class AmbiguousMethodCallException extends RuntimeException {

    public AmbiguousMethodCallException(String message) {
        super(message);
    }

    public AmbiguousMethodCallException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
