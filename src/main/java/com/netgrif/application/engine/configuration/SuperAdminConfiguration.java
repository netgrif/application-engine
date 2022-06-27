package com.netgrif.application.engine.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nae.admin")
public class SuperAdminConfiguration {

    private static SuperAdminConfiguration instance;

    private String password = "password";
    private String email = "super@netgrif.com";
    private String name = "Admin";
    private String surname = "Netgrif";

    public SuperAdminConfiguration() {
        instance = this;
    }
}
