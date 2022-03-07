package com.netgrif.application.engine.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

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
     * Removes token from cache if the state of context object was updated
     * @param token the token string to be removed from cache
     * */
    @Override
    public void clearToken(String token) {
        if (cachedTokens.contains(token))
            this.cachedTokens.remove(token);
    }

}
