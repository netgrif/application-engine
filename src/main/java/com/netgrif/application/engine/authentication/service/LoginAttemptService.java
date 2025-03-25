package com.netgrif.application.engine.authentication.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.netgrif.application.engine.authentication.service.interfaces.ILoginAttemptService;
import com.netgrif.application.engine.configuration.properties.SecurityLimitsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutionException;


@Slf4j
@Service
public class LoginAttemptService implements ILoginAttemptService {

    private final SecurityLimitsProperties securityLimitsProperties;
    private final LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService(SecurityLimitsProperties securityLimitsProperties) {
        this.securityLimitsProperties = securityLimitsProperties;
        this.attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(securityLimitsProperties.getLoginTimeout(), securityLimitsProperties.getLoginTimeoutUnit()).build(new CacheLoader<>() {
                    @Override
                    public Integer load(@Nonnull String key) {
                        return 0;
                    }
                });
    }

    /**
     * todo javadoc
     * */
    public void loginSucceeded(String key) {
        attemptsCache.invalidate(key);
    }

    /**
     * todo javadoc
     * */
    public void loginFailed(String key) {
        int attempts;
        try {
            attempts = attemptsCache.get(key);
        } catch (ExecutionException e) {
            log.error("Error reading login attempts cache for key {}", key, e);
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(key, attempts);
    }

    /**
     * todo javadoc
     * */
    public boolean isBlocked(String key) {
        try {
            return attemptsCache.get(key) >= securityLimitsProperties.getLoginAttempts();
        } catch (ExecutionException e) {
            log.error("Error reading login attempts cache for key {}", key, e);
            return false;
        }
    }
}