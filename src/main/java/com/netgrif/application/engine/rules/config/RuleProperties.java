package com.netgrif.application.engine.rules.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rule-engine")
public class RuleProperties {

    private boolean enabled = false;
    private boolean rethrowExceptions = false;

}
