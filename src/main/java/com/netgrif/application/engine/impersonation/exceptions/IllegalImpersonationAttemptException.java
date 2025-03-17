package com.netgrif.application.engine.impersonation.exceptions;

import com.netgrif.application.engine.authentication.domain.Identity;

public class IllegalImpersonationAttemptException extends Exception {

    public IllegalImpersonationAttemptException(Identity identity, String id) {
        super(identity.getFullName() + " cannot impersonate user or config with ID " + id);
    }

    public IllegalImpersonationAttemptException(String message) {
        super(message);
    }
}
