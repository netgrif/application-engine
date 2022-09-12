package com.netgrif.application.engine.configuration.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.uri")
public class UriProperties {

    private String index = "nae_uri";
    private String separator = "/";

}
