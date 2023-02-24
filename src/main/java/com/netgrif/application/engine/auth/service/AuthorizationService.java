package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorizationService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService implements IAuthorizationService {

    @Autowired
    private IUserService userService;

    @Override
    public boolean hasAuthority(String authority) {
        LoggedUser loggedUser = userService.getLoggedUserFromContext().getSelfOrImpersonated();
        return loggedUser.getAuthorities().stream().anyMatch(it -> it.getAuthority().equals(Authority.ROLE + authority));
    }
}
