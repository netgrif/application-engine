package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;

public interface SecurityContextService {

    void saveToken(String token);

    void reloadSecurityContext(LoggedUser loggedUser);

    void forceReloadSecurityContext(LoggedUser loggedUser);

    boolean isAuthenticatedPrincipalLoggedUser();
}
