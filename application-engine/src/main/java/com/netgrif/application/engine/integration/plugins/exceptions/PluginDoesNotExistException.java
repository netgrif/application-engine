package com.netgrif.application.engine.integration.plugins.exceptions;

public class PluginDoesNotExistException extends RuntimeException {
    public PluginDoesNotExistException(String message) {
        super(message);
    }

    public PluginDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
