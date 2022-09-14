package com.netgrif.application.engine.auth.domain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "nae.authority")
public class AuthorityProperties {

    private List<String> defaultUserAuthorities;

    private List<String> defaultAnonymousAuthorities;

    private List<String> defaultAdminAuthorities;
}
