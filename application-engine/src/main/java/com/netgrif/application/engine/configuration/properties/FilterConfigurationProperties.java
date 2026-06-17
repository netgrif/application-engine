package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for filtering functionality within the Netgrif Application Engine.
 * Provides options to customize export settings and enable or disable default filters.
 */
@Data
@ConfigurationProperties(prefix = "netgrif.engine.filter")
public class FilterConfigurationProperties {

    /**
     * Configuration for filter export settings, such as file name.
     */
    private ExportProperties export = new ExportProperties();

    /**
     * Indicates whether default filters should be created.
     * Defaults to {@code false}.
     */
    private boolean createDefaultFilters = false;

    /**
     * Nested configuration properties for filter export options.
     * Allows customization of export behavior, such as defining a file name.
     */
    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.filter.export")
    public static class ExportProperties {

        /**
         * The name of the file used for exporting filters.
         * Defaults to {@code "filters.xml"}.
         */
        private String fileName = "filters.xml";
    }

}