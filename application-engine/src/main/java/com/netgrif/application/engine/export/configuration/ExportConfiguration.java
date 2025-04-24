package com.netgrif.application.engine.export.configuration;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.export")
public class ExportConfiguration {

    private int mongoPageSize = 100;

    private int elasticPageSize = 100;

}
