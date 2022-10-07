package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.impersonation")
public class ImpersonationProperties {

    private String redisNamespace;
    private boolean enabled;
    private int configsPerUser = 1000;

}
