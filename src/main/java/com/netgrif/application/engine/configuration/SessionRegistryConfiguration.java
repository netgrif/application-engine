package com.netgrif.application.engine.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

@Configuration
public class SessionRegistryConfiguration {

    @Value("${nae.impersonation.redis.namespace}")
    private String namespace;

    @Bean
    public SpringSessionBackedSessionRegistry<? extends Session> springSessionBackedSessionRegistry(FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
        return new SpringSessionBackedSessionRegistry<>(sessionRepository);
    }

    @Bean
    public String impersonatorRedisHash() {
        return namespace;
    }

}
