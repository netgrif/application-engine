package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.domain.AuthorityEnum;
import com.netgrif.application.engine.auth.service.interfaces.IBaseAuthorizationService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;

public abstract class AbstractBaseAuthorizationService implements IBaseAuthorizationService {

    private final IUserService userService;

    public AbstractBaseAuthorizationService(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public final boolean hasAuthority(AuthorityEnum authority) {
        return this.userService.getLoggedUser().getAuthorities().stream().anyMatch(a -> a.includes(authority));
    }
}
