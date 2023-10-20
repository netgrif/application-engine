package com.netgrif.application.engine.integration.plugins.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nae.plugin.registration")
public class PluginRegistrationConfigProperties {
    private String url;
    private int port;
}
