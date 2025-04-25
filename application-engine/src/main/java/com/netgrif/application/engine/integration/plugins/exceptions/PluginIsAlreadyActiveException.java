package com.netgrif.application.engine.integration.plugins.exceptions;

public class PluginIsAlreadyActiveException extends RuntimeException {

    public PluginIsAlreadyActiveException(String message) {
        super(message);
    }

    public PluginIsAlreadyActiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
