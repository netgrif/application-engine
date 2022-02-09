package com.netgrif.application.engine.configuration.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "nae.session")
@Data
@Component
public class SessionUtilsProperties {
    private boolean enabledLimitSession = false;
    private int maxSession = 1;
    private boolean enabledFilter = false;
}