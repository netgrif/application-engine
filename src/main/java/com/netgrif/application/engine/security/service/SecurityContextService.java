package com.netgrif.application.engine.security.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.UserDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Service for managing security context object, like user resource
 * */
@Slf4j
@Service
public class SecurityContextService implements ISecurityContextService{

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * List containing user IDs that's state was changed during an action
     * */
    private final Set<String> cachedTokens;

    protected SecurityContextService() {
        this.cachedTokens = new HashSet<>();
    }

    /**
     * Saves token to the cache if the state of context object was changed and needs to be updated
     * @param token the token string to be cached
     * */
    @Override
    public void saveToken(String token) {
        if (!cachedTokens.contains(token))
            this.cachedTokens.add(token);
    }

    /**
     * Checks if logged user has cached token and needs to be refreshed
     * @param loggedUser currently logged user
     * */
    @Override
    public void reloadLoggedUserContext(LoggedUser loggedUser) {
        if (cachedTokens.contains(loggedUser.getId())) {
            userDetailsService.reloadSecurityContext(loggedUser);
            cachedTokens.remove(loggedUser.getId());
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
}
