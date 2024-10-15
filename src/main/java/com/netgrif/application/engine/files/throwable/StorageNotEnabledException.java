package com.netgrif.application.engine.files.throwable;

public class StorageNotEnabledException extends RuntimeException {
    private static final long serialVersionUID = 7462958789076658518L;
    public StorageNotEnabledException(String message) {
        super(message);
    }
}
