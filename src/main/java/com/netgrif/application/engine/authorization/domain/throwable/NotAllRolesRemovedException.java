package com.netgrif.application.engine.authorization.domain.throwable;

public class NotAllRolesRemovedException extends RuntimeException {

    public NotAllRolesRemovedException(int numberOfRolesNotAssigned) {
        super(String.format("%d role assignments were not removed", numberOfRolesNotAssigned));
    }
}
