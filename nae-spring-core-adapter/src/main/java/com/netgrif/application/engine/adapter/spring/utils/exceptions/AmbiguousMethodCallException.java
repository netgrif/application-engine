package com.netgrif.application.engine.adapter.spring.utils.exceptions;

import java.io.Serial;

/**
 * Exception thrown when a method call cannot be resolved unambiguously
 * due to multiple possible matches.
 */
public final class AmbiguousMethodCallException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message a description of the ambiguity that caused the exception
     */
    public AmbiguousMethodCallException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message   a description of the ambiguity that caused the exception
     * @param throwable the cause of the exception
     */
    public AmbiguousMethodCallException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
