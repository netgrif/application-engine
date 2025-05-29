package com.netgrif.application.engine.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "role")
public class RBACProperties {
    private Duration defaultAssignmentSessionDuration;
}
