package com.netgrif.application.engine.security.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.UserDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing security context object, like user resource
 * */
@Slf4j
@Service
public class SecurityContextService implements ISecurityContextService{

    /**
     * List containing user IDs that's state was changed during an action
     * */
    private final Set<String> cachedTokens;

    protected SecurityContextService() {
        this.cachedTokens = ConcurrentHashMap.newKeySet();
    }

    /**
     * Saves token to the cache if the state of context object was changed and needs to be updated
     * @param token the token string to be cached
     * */
    @Override
    public final void saveToken(String token) {
        if (!cachedTokens.contains(token))
            this.cachedTokens.add(token);
    }

    /**
     * Checks if logged user has cached token and needs to be refreshed
     * @param loggedUser currently logged user
     * */
    @Override
    public final void reloadLoggedUserContext(LoggedUser loggedUser) {
        if (cachedTokens.contains(loggedUser.getId())) {
            reloadSecurityContext(loggedUser);
        }
    }

    /**
     * Reloads the security context according to currently logged user
     * @param loggedUser the user whose context needs to be reloaded
     * */
    @Override
    public final void reloadSecurityContext(LoggedUser loggedUser) {
        if (isUserLogged(loggedUser)) {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loggedUser, SecurityContextHolder.getContext().getAuthentication().getCredentials(), loggedUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(token);
            clearToken(loggedUser.getId());
        }
    }

    /**
     * Removes token from cache if the state of context object was updated
     * @param token the token string to be removed from cache
     * */
    private void clearToken(String token) {
        if (cachedTokens.contains(token))
            this.cachedTokens.remove(token);
    }

    /**
     * Checks whether the user is logged in
     * @param loggedUser the user that is needed to be checked
     * @return true if logged user is in the security context
     * */
    private boolean isUserLogged(LoggedUser loggedUser) {
        if (SecurityContextHolder.getContext().getAuthentication() != null && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof LoggedUser)
            return ((LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId().equals(loggedUser.getId());
        else
            return false;
    }
}
