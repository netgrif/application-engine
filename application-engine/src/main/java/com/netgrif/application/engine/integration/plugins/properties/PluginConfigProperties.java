package com.netgrif.application.engine.integration.plugins.properties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConditionalOnProperty(
        value = "nae.plugin.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@ConfigurationProperties(prefix = "nae.plugin")
public class PluginConfigProperties {
    private boolean enabled;
    private int port;
}
