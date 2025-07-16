package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for managing plugins in the application.
 * <p>This class is used to configure the behavior of plugins using the prefix {@code netgrif.engine.plugin}.</p>
 */
@Data
@ConfigurationProperties(prefix = "netgrif.engine.plugin")
public class PluginConfigurationProperties {

    /**
     * Indicates if plugins are enabled.
     * <p>Set to {@code true} to enable plugins, or {@code false} to disable them. Default is {@code true}.</p>
     */
    private boolean enabled = true;
}
