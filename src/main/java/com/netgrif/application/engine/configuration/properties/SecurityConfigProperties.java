package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.server.security")
public class SecurityConfigProperties {

    /**
     * Defines whether Cross Site Request Forgery is enabled
     */
    private boolean csrf = true;

    /**
     * Enable CORS (Cross-Origin Resource)
     */
    private boolean cors = true;

    /**
     * List of Origins for CORS
     * Example: nae.server.security.allowed-origins=http://localhost:4200
     */
    private List<String> allowedOrigins;


    /**
     * Headers settings
     */
    private Headers headers;

}
