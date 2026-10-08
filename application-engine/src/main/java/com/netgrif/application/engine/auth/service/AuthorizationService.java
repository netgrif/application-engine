package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.service.interfaces.IAuthorizationService;

import java.util.Arrays;
import java.util.Set;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService implements IAuthorizationService {

    @Autowired
    private UserService userService;

    @Override
    public boolean hasAuthority(String authority) {
        // TODO: impersonation
//        LoggedUser loggedUser = userService.getLoggedUserFromContext().getSelfOrImpersonated();
        LoggedUser loggedUser = userService.getLoggedUserFromContext();
        return loggedUser.getAuthoritySet().stream().anyMatch(it -> it.getAuthority().equals(authority));
    }

    @Override
    public boolean hasAnyAuthority(String... authority) {
        // TODO: impersonation
//        LoggedUser loggedUser = userService.getLoggedUserFromContext().getSelfOrImpersonated();
        LoggedUser loggedUser = userService.getLoggedUserFromContext();
        Set<String> authoritySet = Set.of(authority);
        return loggedUser.getAuthoritySet().stream().anyMatch(it -> authoritySet.contains(it.getAuthority()));
    }
}
