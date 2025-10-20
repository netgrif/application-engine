package com.netgrif.application.engine.adapter.spring.utils.exceptions;

import java.io.Serial;

public final class AmbiguousMethodCallException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AmbiguousMethodCallException(String message) {
        super(message);
    }

    public AmbiguousMethodCallException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
