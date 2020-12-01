package com.netgrif.workflow.mail;
import java.util.concurrent.ExecutionException;

import com.netgrif.workflow.configuration.properties.ConfigurationProps;
import com.netgrif.workflow.mail.interfaces.IMailAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class MailAttemptService implements IMailAttemptService {


    private ConfigurationProps configurationProps;

    static final Logger log = LoggerFactory.getLogger(MailAttemptService.class);

    private LoadingCache<String, Integer> attemptsCache;

    @Autowired
    public MailAttemptService(ConfigurationProps configurationProps) {
          super();
          this.configurationProps = configurationProps;
          attemptsCache = CacheBuilder.newBuilder().
                expireAfterWrite(configurationProps.getEmailBlockDuration(), configurationProps.getEmailBlockTimeType()).build(new CacheLoader<String, Integer>() {
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
            return attemptsCache.get(key) >= configurationProps.getEmailSendsAttempts();
        } catch (ExecutionException e) {
            log.error("Error reading mail attempts cache for key " + key , e);
            return false;
        }
    }
}
