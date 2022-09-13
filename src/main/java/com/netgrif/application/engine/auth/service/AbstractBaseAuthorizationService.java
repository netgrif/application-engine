package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.service.interfaces.IBaseAuthorizationService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractBaseAuthorizationService implements IBaseAuthorizationService {

    private final IUserService userService;

    public AbstractBaseAuthorizationService(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public final boolean hasAnyAuthority(String[] authorizingObject) {
        if (authorizingObject == null || authorizingObject.length == 0)
            return true;
        Set<String> loggedUserAuthorities = this.userService.getLoggedUser().getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        return loggedUserAuthorities.containsAll(Arrays.asList(authorizingObject));
    }
}
