package com.netgrif.application.engine.impersonation.exceptions;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;

public class IllegalImpersonationAttemptException extends Exception {

    public IllegalImpersonationAttemptException(LoggedUser loggedUser, String id) {
        super(loggedUser.getName() + " cannot impersonate user or config with ID " + id);
    }

    public IllegalImpersonationAttemptException(String message) {
        super(message);
    }
}
