package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authorization.service.interfaces.IAuthorizationService;
import com.netgrif.application.engine.authentication.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService implements IAuthorizationService {

    @Autowired
    private IUserService userService;

    @Override
    public boolean hasAuthority(String authority) {
        Identity identity = userService.getLoggedUserFromContext().getSelfOrImpersonated();
        return identity.getAuthorities().stream().anyMatch(it -> it.getAuthority().equals(SessionRole.ROLE + authority));
    }
}
