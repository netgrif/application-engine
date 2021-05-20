package com.netgrif.workflow.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "nae.oauth")
@Data
@Component
public class NaeOAuthProperties {

    public boolean enabled;
    private String systemUsername;
    private String superUsername;
    private boolean keycloak;

    private MapperProperties mapper;

}