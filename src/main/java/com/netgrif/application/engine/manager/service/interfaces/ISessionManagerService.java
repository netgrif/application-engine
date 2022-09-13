package com.netgrif.application.engine.manager.service.interfaces;

import com.netgrif.application.engine.auth.domain.LoggedUser;

import java.util.Collection;

public interface ISessionManagerService {

    Collection<LoggedUser> getAllLoggedUsers();

    boolean logoutSession(String username);

    boolean logoutAllSession();
}
