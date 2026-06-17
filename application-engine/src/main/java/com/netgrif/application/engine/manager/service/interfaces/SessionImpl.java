package com.netgrif.application.engine.manager.service.interfaces;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

@Configuration
public class SessionImpl {

    // TODO 2025-04-23 vyriešiť toto dvojité session registry lebo táto bola potrebná aby bolo možné získať všetky session ale bije s druhou, ktorá podporuje klastrové sessions
//    @Bean
//    SessionRegistry sessionRegistry() {
//        return new SessionRegistryImpl();
//    }
}
