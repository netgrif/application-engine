package com.netgrif.workflow.mail;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.netgrif.workflow.mail.interfaces.IMailAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import javax.annotation.Nonnull;

@Service
public class MailAttemptService implements IMailAttemptService {

    static final Logger log = LoggerFactory.getLogger(MailAttemptService.class);

    @Value("${spring.max.emailSendsAttempts}")
    private int MAX_ATTEMPT;

    private LoadingCache<String, Integer> attemptsCache;

    public MailAttemptService(@Value("${spring.max.emailBlockDuration}") final int BLOCK_DURATION, @Value("${spring.max.emailBlockTimeType}") @Nonnull final TimeUnit BLOCK_TIME_TYPE) {
          super();
          attemptsCache = CacheBuilder.newBuilder().
                expireAfterWrite(BLOCK_DURATION, BLOCK_TIME_TYPE).build(new CacheLoader<String, Integer>() {
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
            log.error("Error reading mail attempts cache for key " + key , e);
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(String key) {
        try {
            return attemptsCache.get(key) >= MAX_ATTEMPT;
        } catch (ExecutionException e) {
            log.error("Error reading mail attempts cache for key " + key , e);
            return false;
        }
    }
}
