package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.service.interfaces.IUserService;

public abstract class AbstractBaseAuthorizationService {

    private final IUserService userService;

    public AbstractBaseAuthorizationService(IUserService userService) {
        this.userService = userService;
    }

    public boolean hasAuthority(String authority) {
        return this.userService.getLoggedUser().getAuthorities().stream().anyMatch(a -> a.getName().equals(authority));
    }
}
