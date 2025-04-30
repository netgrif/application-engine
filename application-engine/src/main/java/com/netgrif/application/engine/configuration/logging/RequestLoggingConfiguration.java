package com.netgrif.application.engine.configuration.logging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@ConditionalOnProperty(value = "logging.endpoints")
@Configuration
public class RequestLoggingConfiguration {

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        return new ControllerRequestLoggingFilter();
    }
}