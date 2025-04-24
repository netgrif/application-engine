package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.security.limits")
public class SecurityLimitsProperties {

    private int loginAttempts = 10;

    private int loginTimeout = 10;

    private TimeUnit loginTimeoutUnit = TimeUnit.MINUTES;

    private int emailSendsAttempts = 2;

    private int emailBlockDuration = 1;

    private TimeUnit emailBlockTimeType = TimeUnit.DAYS;
}
