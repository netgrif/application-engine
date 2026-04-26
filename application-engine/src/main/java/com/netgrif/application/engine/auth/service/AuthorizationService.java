package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.service.interfaces.IAuthorizationService;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService implements IAuthorizationService {

    @Autowired
    private UserService userService;

    @Override
    public boolean hasAuthority(String authority) {
        LoggedUser loggedUser = userService.getLoggedUserFromContext();
        return loggedUser.getAuthoritySet().stream().anyMatch(it -> it.getAuthority().equals(authority));
    }
}
