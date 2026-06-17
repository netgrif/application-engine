package com.netgrif.application.engine.impersonation.service.interfaces;

public interface IImpersonationSessionService {
    boolean existsSession(String username);

    boolean isImpersonated(String userId);
}
