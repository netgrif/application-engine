package com.netgrif.application.engine.manager.service.interfaces;

import com.netgrif.application.engine.auth.domain.LoggedUser;

import java.util.Collection;

public interface ISessionManagerService {

    Collection<LoggedUser> getAllLoggedUsers();

    void logoutSessionByUsername(String username);

    void logoutAllSession();
}
