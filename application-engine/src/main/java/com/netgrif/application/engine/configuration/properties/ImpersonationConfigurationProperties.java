package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "netgrif.engine.impersonation")
public class ImpersonationConfigurationProperties {

    @Value("#{redisProperties.namespace}")
    private String redisNamespace;
    private boolean enabled;
    private int configsPerUser = 1000;

}
