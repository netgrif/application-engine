package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;

@Data
@Primary
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "netgrif.engine.jackson")
public class JacksonConfigurationProperties extends JacksonProperties {
}
