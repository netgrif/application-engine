package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.adapter.spring.event.NetgrifEventPublisher;
import com.netgrif.application.engine.auth.service.TenantService;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.event.enricher.TenantEventEnricher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventConfiguration {

    @Bean
    public ApplicationEventPublisher netgrifEventPublisher(NetgrifEventPublisher publisher, TenantEventEnricher tenantEventEnricher) {
        publisher.addBeforePublish(tenantEventEnricher);
        return publisher;
    }

    @Bean
    public TenantEventEnricher tenantEnricher(UserService userService, TenantService tenantService) {
        return new TenantEventEnricher(userService, tenantService);
    }


}
