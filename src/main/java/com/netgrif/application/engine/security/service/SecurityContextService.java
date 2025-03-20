package com.netgrif.application.engine.security.service;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.service.IdentityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * todo javadoc everywhere
 * Service for managing security context object, like user resource
 */
@Slf4j
@Service
public class SecurityContextService implements ISecurityContextService {

    /**
     * List containing user IDs that's state was changed during an action
     */
    private final Set<String> cachedTokens;
    private final IdentityService identityService;

    protected SecurityContextService(IdentityService identityService) {
        this.cachedTokens = ConcurrentHashMap.newKeySet();
        this.identityService = identityService;
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
    public void reloadSecurityContext(LoggedIdentity identity) {
        reloadSecurityContext(identity, false);
    }

    /**
     * Reloads the security context according to currently logged user from database
     *
     * @param loggedUser the user whose context needs to be reloaded
     */
    @Override
    public void forceReloadSecurityContext(LoggedIdentity identity) {
        reloadSecurityContext(identity, true);
    }

    /**
     * Checks type of SecurityContext
     *
     * @return true if the SecurityContext exists and is of type LoggedUser
     */
    @Override
    public boolean isAuthenticatedPrincipalLoggedIdentity() {
        return SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof LoggedIdentity;
    }

    private void reloadSecurityContext(LoggedIdentity identity, boolean forceRefresh) {
        if (isUserLogged(identity) && cachedTokens.contains(identity.getIdentityId())) {
            if (forceRefresh) {
                identity = updateSessionFromDatabase(identity);
            }
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(identity,
                    SecurityContextHolder.getContext().getAuthentication().getCredentials(), identity.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(token);
            clearToken(identity.getIdentityId());
        }
    }

    /**
     * todo javadoc
     * */
    private LoggedIdentity updateSessionFromDatabase(LoggedIdentity identity) {
        Optional<Identity> identityOpt = identityService.findById(identity.getIdentityId());
        if (identityOpt.isPresent()) {
            String activeActorId = identity.getActiveActorId();
            identity = identityOpt.get().toSession();
            if (activeActorId != null) {
                identity.setActiveActorId(activeActorId);
            }
            return identity;
        }
        return identity;
    }

    /**
     * Removes token from cache if the state of context object was updated
     *
     * @param token the token string to be removed from cache
     */
    private void clearToken(String token) {
        if (cachedTokens.contains(token)){
            this.cachedTokens.remove(token);
        }
    }

    /**
     * Checks whether the user is logged in
     *
     * @param identity the user that is needed to be checked
     * @return true if logged user is in the security context
     */
    private boolean isUserLogged(LoggedIdentity identity) {
        return isAuthenticatedPrincipalLoggedIdentity()
                && ((LoggedIdentity) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                    .getIdentityId().equals(identity.getIdentityId());
    }
}
