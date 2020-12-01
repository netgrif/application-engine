package com.netgrif.workflow.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.max")
public class BruteForceProperties {

    private int loginAttempts = 10;

    private int emailSendsAttempts = 2;

    private int emailBlockDuration = 1;

    private TimeUnit emailBlockTimeType = TimeUnit.DAYS;
}
