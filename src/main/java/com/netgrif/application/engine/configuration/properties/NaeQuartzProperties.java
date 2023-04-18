package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nae.quartz")
public class NaeQuartzProperties {

    private String dbName;

    private QuartzSchedulerProperties scheduler;
}
