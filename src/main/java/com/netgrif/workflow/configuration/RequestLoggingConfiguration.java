package com.netgrif.workflow.configuration;

import com.netgrif.workflow.ControllerRequestLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingConfiguration {

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        ControllerRequestLoggingFilter filter = new ControllerRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludeHeaders(true);
        filter.setBeforeMessagePrefix("");
        filter.setBeforeMessageSuffix("");
        return filter;
    }
}