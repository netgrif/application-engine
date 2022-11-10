package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "nae.database")
@Data
@Component
public class DatabaseProperties {
    private String password;
    private String algorithm = "PBEWITHSHA256AND256BITAES-CBC-BC";
    private String encryptionPrefix = "#encrypted";
}
