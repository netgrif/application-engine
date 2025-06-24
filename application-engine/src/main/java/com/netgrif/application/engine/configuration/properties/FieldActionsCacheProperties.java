package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.field-runner.cache-size")
public class FieldActionsCacheProperties {

    private Long actions = 500L;

    private Long functions = 500L;

    private Long namespaceFunctions = 500L;
}
