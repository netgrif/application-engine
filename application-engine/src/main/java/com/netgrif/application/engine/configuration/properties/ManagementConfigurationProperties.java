package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.logging.LogFileWebEndpointProperties;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for managing various aspects of the Netgrif Application Engine.
 * Provides hierarchical configuration for endpoints, management details, and health checks.
 */
@Data
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "netgrif.engine.management")
public class ManagementConfigurationProperties {

    /**
     * Configuration properties for managing web endpoints.
     */
    private EndpointsProperties endpoints = new EndpointsProperties();

    /**
     * Configuration for a specific management endpoint.
     */
    private EndpointProperties endpoint = new EndpointProperties();

    /**
     * Build information for the application.
     * This is a mandatory dependency when constructing the class.
     */
    private final BuildProperties info;

    /**
     * Encapsulates properties related to web endpoint management.
     */
    @Data
    @Component
    @ConfigurationProperties(prefix = "netgrif.engine.management.endpoints")
    public static class EndpointsProperties {

        /**
         * Configuration for web endpoints, including base path settings.
         */
        private WebEndpointProperties web = new WebEndpointProperties();
    }

    /**
     * Details configuration for individual management endpoints, such as health and log file endpoints.
     */
    @Data
    @Component
    @ConfigurationProperties(prefix = "netgrif.engine.management.endpoint")
    public static class EndpointProperties {

        /**
         * Configuration for the health endpoint, including configuration toggles.
         */
        private HealthEndpointProperties health = new HealthEndpointProperties();

        /**
         * Configuration for the log file endpoint.
         */
        private LogFileWebEndpointProperties logFile = new LogFileWebEndpointProperties();
    }

    /**
     * Health check properties for the application, enabling configuration of specific health checks.
     */
    @Data
    @Component
    @ConfigurationProperties(prefix = "netgrif.engine.management.health")
    public static class HealthProperties {

        /**
         * Configuration for mail health checks.
         */
        private MailHealthProperties mail = new MailHealthProperties();

        /**
         * Configuration for LDAP health checks.
         */
        private LdapHealthProperties ldap = new LdapHealthProperties();

        /**
         * Specific configuration properties for mail health checks.
         * Allows enabling or disabling mail health verification.
         */
        @Data
        public static class MailHealthProperties {

            /**
             * Whether the mail health check is enabled.
             * Default value is {@code false}.
             */
            private boolean enabled = false;
        }

        /**
         * Specific configuration properties for LDAP health checks.
         * Allows enabling or disabling LDAP health verification.
         */
        @Data
        public static class LdapHealthProperties {

            /**
             * Whether the LDAP health check is enabled.
             * Default value is {@code false}.
             */
            private boolean enabled = false;
        }
    }

}
