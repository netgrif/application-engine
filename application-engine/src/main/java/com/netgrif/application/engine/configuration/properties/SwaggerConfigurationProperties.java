package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "netgrif.engine.swagger")
public class SwaggerConfigurationProperties extends SwaggerUiConfigProperties {
}
