package com.netgrif.application.engine.impersonation.exceptions;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import lombok.Getter;

public class ImpersonatedUserHasSessionException extends Exception {

    @Getter
    private final boolean isImpersonated;

    public ImpersonatedUserHasSessionException(LoggedUser impersonatedLogged, boolean isImpersonated) {
        super(impersonatedLogged.getFullName() + " has an existing session!");
        this.isImpersonated = isImpersonated;
    }
}
