package com.netgrif.application.engine.impersonation.exceptions;

import com.netgrif.application.engine.authentication.domain.Identity;
import lombok.Getter;

public class ImpersonatedUserHasSessionException extends Exception {

    @Getter
    private final boolean isImpersonated;

    public ImpersonatedUserHasSessionException(Identity impersonatedLogged, boolean isImpersonated) {
        super(impersonatedLogged.getFullName() + " has an existing session!");
        this.isImpersonated = isImpersonated;
    }
}
