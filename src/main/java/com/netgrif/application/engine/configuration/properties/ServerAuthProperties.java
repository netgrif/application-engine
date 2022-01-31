package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nae.server.auth")
public class ServerAuthProperties {

    private boolean openRegistration = true;

    private int tokenValidityPeriod = 3;

    private int minimalPasswordLength = 6;

    private boolean enableProfileEdit = true;

    private String[] noAuthenticationPatterns = new String[0];
}
