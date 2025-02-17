package com.netgrif.application.engine.menu.registry.throwable;

public class DuplicateViewException extends RuntimeException{

    public DuplicateViewException(String viewIdentifier) {
        super(String.format("View with identifier [%s] is already registered.", viewIdentifier));
    }
}
