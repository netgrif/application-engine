package com.netgrif.application.engine.authorization.domain.throwable;

public class NotAllRolesAssignedException extends RuntimeException {

    public NotAllRolesAssignedException(int numberOfRolesNotAssigned) {
        super(String.format("%d roles were not assigned", numberOfRolesNotAssigned));
    }
}
