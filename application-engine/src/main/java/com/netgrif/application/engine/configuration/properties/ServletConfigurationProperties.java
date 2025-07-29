package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;

/**
 * Configuration properties for servlet settings of the Netgrif application engine.
 * <p>
 * Provides customization for servlet behaviors like multipart file upload.
 */
@Data
@ConfigurationProperties(prefix = "netgrif.engine.servlet")
public class ServletConfigurationProperties {

    /**
     * Configuration properties for multipart file upload.
     */
    private MultipartProperties multipart = new MultipartProperties();

    /**
     * Nested properties specifically for configuring multipart file upload settings.
     * <p>
     * Extends Spring Boot's default multipart properties to allow additional customization.
     */
    @Data
    @Primary
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @ConfigurationProperties(prefix = "netgrif.engine.servlet.multipart")
    public static class MultipartProperties extends org.springframework.boot.autoconfigure.web.servlet.MultipartProperties {
    }
}
