package com.netgrif.application.engine.manager.service.interfaces;

import com.netgrif.application.engine.authentication.domain.Identity;

import java.util.Collection;

public interface ISessionManagerService {

    Collection<Identity> getAllLoggedUsers();

    void logoutSessionByUsername(String username);

    void logoutAllSession();
}
