package com.netgrif.workflow.orgstructure.groups.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "nae.group")
public class GroupConfigurationProperties {

    private Boolean defaultEnabled;

    private Boolean systemEnabled;
}
