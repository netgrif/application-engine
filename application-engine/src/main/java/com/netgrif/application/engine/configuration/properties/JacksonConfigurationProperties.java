package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;

/**
 * Extension of {@link JacksonProperties} that allows for additional configuration through application properties.
 * This class binds properties with the prefix "netgrif.engine.jackson" to customize Jackson's behavior
 * in the Netgrif Application Engine.
 */
@Data
@Primary
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "netgrif.engine.jackson")
public class JacksonConfigurationProperties extends JacksonProperties {
}
