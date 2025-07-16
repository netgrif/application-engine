package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * Configuration properties for the dashboard in the Netgrif Application Engine.
 * Allows toggling dashboard functionality via application properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "netgrif.engine.dashboard")
public class DashboardProperties {

    /**
     * Determines whether the dashboard is enabled or disabled.
     * Default value is {@code false}.
     */
    private Boolean enabled = false;

}
