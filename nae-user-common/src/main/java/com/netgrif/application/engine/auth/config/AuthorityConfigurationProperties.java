package com.netgrif.application.engine.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "netgrif.engine.authority")
public class AuthorityConfigurationProperties {

    private List<String> defaultUserAuthorities;

    private List<String> defaultAnonymousAuthorities;

    private List<String> defaultAdminAuthorities;
}
