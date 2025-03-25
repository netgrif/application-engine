package com.netgrif.application.engine.security.service;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;

public interface ISecurityContextService {

    void saveToken(String token);

    void reloadSecurityContext(LoggedIdentity identity);

    void forceReloadSecurityContext(LoggedIdentity identity);

    boolean isAuthenticatedPrincipalLoggedIdentity();

    boolean isIdentityLogged(String identityId);
}
