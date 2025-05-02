package com.netgrif.application.engine.manager.service.interfaces;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;

import java.util.Collection;

public interface ISessionManagerService {

    LoggedIdentity getLoggedIdentity();

    LoggedIdentity getLoggedSystemIdentity();

    String getActiveActorId();

    Collection<LoggedIdentity> getAllLoggedIdentities();

    void logoutSessionByUsername(String username);

    void logoutAllSession();
}
