package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.UriProperties;
import com.netgrif.application.engine.petrinet.domain.repository.UriNodeRepository;
import com.netgrif.application.engine.petrinet.service.UriService;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UriServiceConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IUriService uriService(UriNodeRepository uriNodeRepository, UriProperties uriProperties) {
        return new UriService(uriNodeRepository, uriProperties);
    }
}
