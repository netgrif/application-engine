package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.auth.service.interfaces.IBaseAuthorizationService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;

public abstract class AbstractBaseAuthorizationService implements IBaseAuthorizationService {

    private final IUserService userService;
    private final IAuthorityService authorityService;

    public AbstractBaseAuthorizationService(IUserService userService, IAuthorityService authorityService) {
        this.userService = userService;
        this.authorityService = authorityService;
    }

    @Override
    public final boolean hasAuthority(String authorizingObject) {
        if (authorizingObject == null || authorizingObject.length() == 0)
            return true;
        return this.userService.getLoggedUser().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(authorizingObject));
    }
}
