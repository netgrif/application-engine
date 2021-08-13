package com.netgrif.workflow.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.field-runner.cache-size")
public class FieldActionsCacheProperties {

    private Long actions;

    private Long functions;

    private Long namespaceFunctions;
}
