package com.netgrif.application.engine.adapter.spring.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for impersonation functionality in the Netgrif Application Engine.
 * This class allows enabling and configuring impersonation features, including Redis namespace
 * customization and user-specific configurations.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "netgrif.impersonation")
public class ImpersonationConfigurationProperties {

    /**
     * The Redis namespace used for storing impersonation configurations.
     * This value is resolved from the "redisProperties.namespace" SpEL expression.
     */
    @Value("#{redisProperties.session.namespace}")
    private String redisNamespace;

    /**
     * Indicates whether impersonation functionality is enabled.
     */
    private boolean enabled;

}
