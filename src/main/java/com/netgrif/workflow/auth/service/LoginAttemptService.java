package com.netgrif.workflow.auth.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.netgrif.workflow.auth.service.interfaces.ILoginAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


@Service
public class LoginAttemptService implements ILoginAttemptService {

    static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    @Value("${spring.max.login.attempts}")
    private int MAX_ATTEMPT;

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
            log.error(e.toString());
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(String key) {
        try {
            return attemptsCache.get(key) >= MAX_ATTEMPT;
        } catch (ExecutionException e) {
            log.error(e.toString());
            return false;
        }
    }
}