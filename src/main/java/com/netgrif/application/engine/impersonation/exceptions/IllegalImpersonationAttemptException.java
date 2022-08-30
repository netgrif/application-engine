package com.netgrif.application.engine.impersonation.exceptions;

import com.netgrif.application.engine.auth.domain.LoggedUser;

public class IllegalImpersonationAttemptException extends Exception {

    public IllegalImpersonationAttemptException(LoggedUser loggedUser, String userId) {
        super(loggedUser.getFullName() + " cannot impersonate user with ID " + userId);
    }

    public IllegalImpersonationAttemptException(String message) {
        super(message);
    }
}
