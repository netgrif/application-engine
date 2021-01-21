package com.netgrif.workflow.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.security")
public class SecurityConfigProperties {

    /**
     * Defines whether Cross Site Request Forgery is enabled
     * */
    private boolean csrf = true;
}
