package com.netgrif.application.engine.auth.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.netgrif.application.engine.auth.service.interfaces.ILoginAttemptService;
import com.netgrif.application.engine.configuration.properties.SecurityLimitsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;


@Slf4j
@Service
public class LoginAttemptService implements ILoginAttemptService {

    @Autowired
    private SecurityLimitsProperties securityLimitsProperties;

    private LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService(SecurityLimitsProperties securityLimitsProperties) {
        super();
        attemptsCache = CacheBuilder.newBuilder().
                expireAfterWrite(securityLimitsProperties.getLoginTimeout(), securityLimitsProperties.getLoginTimeoutUnit()).build(new CacheLoader<>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }


    public void loginSucceeded(String key) {
        attemptsCache.invalidate(key);
    }

    public void loginFailed(String key) {
        int attempts = 0;
        try {
            attempts = attemptsCache.get(key);
        } catch (ExecutionException e) {
            log.error("Error reading login attempts cache for key " + key, e);
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(String key) {
        try {
            return attemptsCache.get(key) >= securityLimitsProperties.getLoginAttempts();
        } catch (ExecutionException e) {
            log.error("Error reading login attempts cache for key " + key, e);
            return false;
        }
    }
}