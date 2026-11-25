package com.netgrif.application.engine.auth.service;

import java.util.Set;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private final UserService userService;

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
