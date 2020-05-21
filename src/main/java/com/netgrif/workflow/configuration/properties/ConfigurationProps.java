package com.netgrif.workflow.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.TimeUnit;

@Configuration
@PropertySource("classpath:configuration.properties")
@ConfigurationProperties(prefix = "spring.max")
public class ConfigurationProps {
    @Getter @Setter
    private int loginAttempts = 10;

    @Getter @Setter
    private int emailSendsAttempts = 2;

    @Getter @Setter
    private int emailBlockDuration = 1;

    @Getter @Setter
    private TimeUnit emailBlockTimeType = TimeUnit.DAYS;
}
