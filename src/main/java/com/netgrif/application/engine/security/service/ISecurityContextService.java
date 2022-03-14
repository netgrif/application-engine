package com.netgrif.application.engine.security.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;

public interface ISecurityContextService {

    void saveToken(String token);

    void reloadLoggedUserContext(LoggedUser loggedUser);

    void reloadSecurityContext(LoggedUser loggedUser);
}