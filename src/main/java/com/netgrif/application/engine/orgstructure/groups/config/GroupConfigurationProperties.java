package com.netgrif.application.engine.orgstructure.groups.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "nae.group")
public class GroupConfigurationProperties {

    /**
     * Determines whether user's default groups are enabled and can be created
     * */
    private boolean defaultEnabled = true;

    /**
     * Determines whether default system group is enabled and can be created
     * */
    private boolean systemEnabled = true;
}
