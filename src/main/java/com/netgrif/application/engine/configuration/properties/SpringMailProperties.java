package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.mail")
public class SpringMailProperties {

    private String host;

    private int port;

    private String protocol;

    private String testConnection;

    private String defaultEncoding;
}
