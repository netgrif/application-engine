package com.netgrif.application.engine.security.service;

import com.netgrif.core.auth.domain.LoggedUser;

public interface ISecurityContextService {

    void saveToken(String token);

    void reloadSecurityContext(LoggedUser loggedUser);

    void forceReloadSecurityContext(LoggedUser loggedUser);

    boolean isAuthenticatedPrincipalLoggedUser();
}
