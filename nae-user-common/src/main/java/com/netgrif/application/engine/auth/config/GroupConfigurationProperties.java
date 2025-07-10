package com.netgrif.application.engine.auth.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "netgrif.engine.group")
public class GroupConfigurationProperties {

    /**
     * Determines whether user's default groups are enabled and can be created
     */
    private boolean defaultEnabled = true;

    /**
     * Determines whether default system group is enabled and can be created
     */
    private boolean systemEnabled = true;

    private String defaultGroupIdentifier = "Default system group";

    private String defaultGroupTitle = defaultGroupIdentifier;
}
