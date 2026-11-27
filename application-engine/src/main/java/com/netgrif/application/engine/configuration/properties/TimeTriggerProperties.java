package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "netgrif.engine.quartz.time-trigger")
public class TimeTriggerProperties {

    private RedeliverProperties redeliver = new RedeliverProperties();

    @Data
    public static class RedeliverProperties {

        private Duration delay = Duration.ofSeconds(30);

        private int maxAttempts = 3;
    }
}
