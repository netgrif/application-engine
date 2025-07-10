package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "netgrif.engine.postal-code")
public class PostalCodeConfigurationProperties {
    private boolean enableImport = true;
    private String importFilePath = "postal_codes.csv";
}
