package com.netgrif.application.engine.manager.service.interfaces;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

@Configuration
public class SessionImpl {

    @Bean
    SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
}
