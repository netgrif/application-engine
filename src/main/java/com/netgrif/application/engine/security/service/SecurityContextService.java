package com.netgrif.application.engine.security.service;

import com.netgrif.adapter.auth.domain.LoggedUserImpl;
import com.netgrif.core.auth.domain.LoggedUser;
import com.netgrif.auth.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing security context object, like user resource
 */
@Slf4j
@Service
public class SecurityContextService implements ISecurityContextService {

    /**
     * List containing user IDs that's state was changed during an action
     */
    private final Set<String> cachedTokens;

    protected UserService userService;

    protected SecurityContextService() {
        this.cachedTokens = ConcurrentHashMap.newKeySet();
    }

    /**
     * Saves token to the cache if the state of context object was changed and needs to be updated
     *
     * @param token the token string to be cached
     */
    @Override
    public void saveToken(String token) {
        this.cachedTokens.add(token);
    }

    /**
     * Reloads the security context according to currently logged user
     *
     * @param loggedUser the user whose context needs to be reloaded
     */
    @Override
    public void reloadSecurityContext(LoggedUser loggedUser) {
        reloadSecurityContext(loggedUser, false);
    }

    /**
     * Reloads the security context according to currently logged user from database
     *
     * @param loggedUser the user whose context needs to be reloaded
     */
    @Override
    public void forceReloadSecurityContext(LoggedUser loggedUser) {
        reloadSecurityContext(loggedUser, true);
    }

    private void reloadSecurityContext(LoggedUser loggedUser, boolean forceRefresh) {
        if (isUserLogged(loggedUser) && cachedTokens.contains(loggedUser.getId())) {
            if (forceRefresh) {
                loggedUser = (LoggedUser) userService.transformToLoggedUser(userService.findById(loggedUser.getId(), null));
            }
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loggedUser, SecurityContextHolder.getContext().getAuthentication().getCredentials(), ((LoggedUserImpl) loggedUser).getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(token);
            clearToken(loggedUser.getId());
        }
    }

    /**
     * Checks type of SecurityContext
     *
     * @return true if the SecurityContext exists and is of type LoggedUser
     */
    @Override
    public boolean isAuthenticatedPrincipalLoggedUser() {
        return SecurityContextHolder.getContext().getAuthentication() != null && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof LoggedUser;
    }

    /**
     * Removes token from cache if the state of context object was updated
     *
     * @param token the token string to be removed from cache
     */
    private void clearToken(String token) {
        if (cachedTokens.contains(token))
            this.cachedTokens.remove(token);
    }

    /**
     * Checks whether the user is logged in
     *
     * @param loggedUser the user that is needed to be checked
     * @return true if logged user is in the security context
     */
    private boolean isUserLogged(LoggedUser loggedUser) {
        if (isAuthenticatedPrincipalLoggedUser())
            return ((LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId().equals(loggedUser.getId());
        else
            return false;
    }

    @Autowired
    @Lazy
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
