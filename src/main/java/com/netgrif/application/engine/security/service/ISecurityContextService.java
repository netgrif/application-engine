package com.netgrif.application.engine.security.service;

import com.netgrif.application.engine.authentication.domain.Identity;

public interface ISecurityContextService {

    void saveToken(String token);

    void reloadSecurityContext(Identity identity);

    void forceReloadSecurityContext(Identity identity);

    boolean isAuthenticatedPrincipalLoggedUser();
}
