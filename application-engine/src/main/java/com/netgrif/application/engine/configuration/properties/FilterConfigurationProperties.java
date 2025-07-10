package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "netgrif.engine.filter")
public class FilterConfigurationProperties {

    private ExportProperties export = new ExportProperties();

    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.filter.export")
    public static class ExportProperties {
        private String fileName = "filters.xml";
    }

}