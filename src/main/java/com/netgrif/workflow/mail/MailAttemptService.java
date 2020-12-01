package com.netgrif.workflow.mail;
import java.util.concurrent.ExecutionException;

import com.netgrif.workflow.configuration.properties.BruteForceProperties;
import com.netgrif.workflow.mail.interfaces.IMailAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Slf4j
@Service
public class MailAttemptService implements IMailAttemptService {

    private BruteForceProperties bruteForceProperties;

    private LoadingCache<String, Integer> attemptsCache;

    @Autowired
    public MailAttemptService(BruteForceProperties bruteForceProperties) {
          super();
          this.bruteForceProperties = bruteForceProperties;
          attemptsCache = CacheBuilder.newBuilder().
                expireAfterWrite(bruteForceProperties.getEmailBlockDuration(), bruteForceProperties.getEmailBlockTimeType()).build(new CacheLoader<String, Integer>() {
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
            return attemptsCache.get(key) >= bruteForceProperties.getEmailSendsAttempts();
        } catch (ExecutionException e) {
            log.error("Error reading mail attempts cache for key " + key , e);
            return false;
        }
    }
}
