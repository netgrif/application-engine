package com.netgrif.application.engine.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;

@Configuration(proxyBeanMethods = false)
@Profile("test")
public class RedisSessionTestConfiguration {

    @Bean
    SessionRepositoryCustomizer<RedisIndexedSessionRepository> disableRedisSessionCleanup() {
        return repository -> repository.setCleanupCron(Scheduled.CRON_DISABLED);
    }
}
