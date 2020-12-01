package com.netgrif.workflow.auth.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.netgrif.workflow.auth.service.interfaces.ILoginAttemptService;
import com.netgrif.workflow.configuration.properties.BruteForceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


@Slf4j
@Service
public class LoginAttemptService implements ILoginAttemptService {

    @Autowired
    private BruteForceProperties bruteForceProperties;

    private LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        super();
        attemptsCache = CacheBuilder.newBuilder().
                expireAfterWrite(1, TimeUnit.DAYS).build(new CacheLoader<String, Integer>() {
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
            log.error("Error reading login attempts cache for key " + key , e);
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(String key) {
        try {
            return attemptsCache.get(key) >= bruteForceProperties.getLoginAttempts();
        } catch (ExecutionException e) {
            log.error("Error reading login attempts cache for key " + key , e);
            return false;
        }
    }
}