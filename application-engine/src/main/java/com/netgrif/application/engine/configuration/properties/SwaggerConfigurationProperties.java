package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Swagger UI of the Netgrif Engine.
 * <p>
 * This class extends the {@link SwaggerUiConfigProperties} class and allows customization
 * of Swagger UI-related settings for the Netgrif Engine.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "netgrif.engine.swagger")
public class SwaggerConfigurationProperties extends SwaggerUiConfigProperties {
}
