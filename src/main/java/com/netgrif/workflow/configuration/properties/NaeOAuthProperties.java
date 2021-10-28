package com.netgrif.workflow.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nae.oauth")
public class NaeOAuthProperties {

    public boolean enabled = false;
    private boolean keycloak;
    private boolean remoteUserBase;
    private String superUsername;

    private MapperProperties mapper;

}