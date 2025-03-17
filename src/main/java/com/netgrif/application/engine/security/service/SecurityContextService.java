package com.netgrif.application.engine.security.service;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.service.interfaces.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    protected IUserService userService;

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
     * @param identity the user whose context needs to be reloaded
     */
    @Override
    public void reloadSecurityContext(Identity identity) {
        reloadSecurityContext(identity, false);
    }

    /**
     * Reloads the security context according to currently logged user from database
     *
     * @param identity the user whose context needs to be reloaded
     */
    @Override
    public void forceReloadSecurityContext(Identity identity) {
        reloadSecurityContext(identity, true);
    }

    private void reloadSecurityContext(Identity identity, boolean forceRefresh) {
        if (isUserLogged(identity) && cachedTokens.contains(identity.getId())) {
            if (forceRefresh) {
                identity = userService.findById(identity.getId()).transformToLoggedUser();
            }
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(identity, SecurityContextHolder.getContext().getAuthentication().getCredentials(), identity.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(token);
            clearToken(identity.getId());
        }
    }

    /**
     * Checks type of SecurityContext
     *
     * @return true if the SecurityContext exists and is of type LoggedUser
     */
    @Override
    public boolean isAuthenticatedPrincipalLoggedUser() {
        return SecurityContextHolder.getContext().getAuthentication() != null && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof Identity;
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
     * @param identity the user that is needed to be checked
     * @return true if logged user is in the security context
     */
    private boolean isUserLogged(Identity identity) {
        if (isAuthenticatedPrincipalLoggedUser())
            return ((Identity) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId().equals(identity.getId());
        else
            return false;
    }

    @Autowired
    @Lazy
    public void setUserService(IUserService userService) {
        this.userService = userService;
    }
}
