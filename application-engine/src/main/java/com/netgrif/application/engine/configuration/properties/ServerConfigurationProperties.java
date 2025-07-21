package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;

/**
 * Configuration properties for the Netgrif Engine server.
 * <p>
 * Extends Spring Boot's {@link ServerProperties} to provide additional
 * server-specific customization options using the prefix <code>netgrif.engine.server</code>.
 */
@Data
@Primary
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "netgrif.engine.server")
public class ServerConfigurationProperties extends ServerProperties {
}
