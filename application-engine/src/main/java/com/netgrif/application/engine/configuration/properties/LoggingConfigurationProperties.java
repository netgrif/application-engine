package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


/**
 * Configuration properties for logging settings in the application.
 * These properties control various logging behaviors, such as enabling or
 * disabling endpoint logging.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "netgrif.engine.logging")
public class LoggingConfigurationProperties {

    /**
     * Enables or disables logging of REST endpoint requests.
     * When set to {@code true} (default false), all REST endpoint requests are logged.
     * Controlled via the property "logging.endpoints" in the application configuration.
     */
    private boolean endpoints = false;
    private String config;
    private FileProperties file = new FileProperties();
    private Map<String, Object> level = new HashMap<>();

    @Data
    public static class FileProperties {
        private String path;
    }
}
