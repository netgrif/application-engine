package com.netgrif.workflow.auth.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.netgrif.workflow.auth.service.interfaces.ILoginAttemptService;
import com.netgrif.workflow.configuration.properties.ConfigurationProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


@Service
public class LoginAttemptService implements ILoginAttemptService {

    @Autowired
    private ConfigurationProps configurationProps;

    static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

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
            return attemptsCache.get(key) >= configurationProps.getLoginAttempts();
        } catch (ExecutionException e) {
            log.error("Error reading login attempts cache for key " + key , e);
            return false;
        }
    }
}