package com.netgrif.application.engine.authentication.service;

import com.netgrif.application.engine.authentication.domain.Authority;
import com.netgrif.application.engine.authentication.domain.LoggedUser;
import com.netgrif.application.engine.authentication.service.interfaces.IAuthorizationService;
import com.netgrif.application.engine.authentication.service.interfaces.IUserService;
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
