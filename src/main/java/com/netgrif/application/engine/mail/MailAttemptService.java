package com.netgrif.application.engine.mail;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.netgrif.application.engine.configuration.properties.SecurityLimitsProperties;
import com.netgrif.application.engine.mail.interfaces.IMailAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class MailAttemptService implements IMailAttemptService {

    private SecurityLimitsProperties securityLimitsProperties;

    private LoadingCache<String, Integer> attemptsCache;

    @Autowired
    public MailAttemptService(SecurityLimitsProperties securityLimitsProperties) {
        super();
        this.securityLimitsProperties = securityLimitsProperties;
        attemptsCache = CacheBuilder.newBuilder().
                expireAfterWrite(securityLimitsProperties.getEmailBlockDuration(), securityLimitsProperties.getEmailBlockTimeType()).build(new CacheLoader<String, Integer>() {
            public Integer load(String key) {
                return 0;
            }
        });
    }

    public void mailAttempt(String key) {
        int attempts = 0;
        try {
            attempts = attemptsCache.get(key);
        } catch (ExecutionException e) {
            log.error("Error reading mail attempts cache for key " + key, e);
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(String key) {
        try {
            return attemptsCache.get(key) >= securityLimitsProperties.getEmailSendsAttempts();
        } catch (ExecutionException e) {
            log.error("Error reading mail attempts cache for key " + key, e);
            return false;
        }
    }
}
