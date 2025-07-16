package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Netgrif Engine module.
 * <p>
 * This configuration class allows customization of various module-specific features.
 */
@Data
@ConfigurationProperties(prefix = "netgrif.engine.module")
public class ModuleConfigurationProperties {

    /**
     * Service-specific configuration properties.
     */
    private ServiceProperties service = new ServiceProperties();

    /**
     * Service-specific configuration properties.
     * <p>
     * This nested class controls the service-related features by enabling or disabling them.
     */
    @Data
    public static class ServiceProperties {

        /**
         * Flag indicating whether the service is enabled or not.
         * <p>
         * Defaults to <code>true</code>.
         */
        private boolean enabled = true;
    }
}
