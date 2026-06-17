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

    /**
     * Represents a custom configuration string for logging behavior.
     * This property can be used to specify non-standard logging configurations.
     */
    private String config;

    /**
     * Nested configuration properties for file-based logging.
     * These settings allow the specification of file logging paths. Example:
     * {@code netgrif.engine.logging.file.path=/var/logs/app.log}
     */
    private FileProperties file = new FileProperties();

    /**
     * Custom logging levels for various parts of the application.
     * Allows overriding logging levels for specific classes or packages. Example:
     * {@code netgrif.engine.logging.level.com.netgrif=DEBUG}
     */
    private Map<String, Object> level = new HashMap<>();

    /**
     * Nested properties that define settings for file-based logging.
     */
    @Data
    public static class FileProperties {

        /**
         * Specifies the path to the log file where logs will be written.
         * Example:
         * {@code /var/logs/application.log}
         */
        private String path;
    }
}
