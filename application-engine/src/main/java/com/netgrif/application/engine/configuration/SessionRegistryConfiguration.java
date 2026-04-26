package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.adapter.spring.configuration.ImpersonationConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

@Configuration
public class SessionRegistryConfiguration {

    @Autowired
    private ImpersonationConfigurationProperties impersonationProperties;

    @Bean
    public SpringSessionBackedSessionRegistry<? extends Session> springSessionBackedSessionRegistry(FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
        return new SpringSessionBackedSessionRegistry<>(sessionRepository);
    }

    @Bean
    public String impersonatorRedisHash() {
        return impersonationProperties.getRedisNamespace();
    }

}
