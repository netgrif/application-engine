package com.netgrif.workflow.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "nae.ldap")
public class NaeLdapProperties {

    private boolean enabled = false;

    private String superUsername;

    private String userFilter;

    private String base;

    private String groupSearchBase;


}

