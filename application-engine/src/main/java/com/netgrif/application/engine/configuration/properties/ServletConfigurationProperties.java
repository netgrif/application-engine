package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;

@Data
@ConfigurationProperties(prefix = "netgrif.engine.servlet")
public class ServletConfigurationProperties {
    private MultipartProperties multipart = new MultipartProperties();

    @Data
    @Primary
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @ConfigurationProperties(prefix = "netgrif.engine.servlet.multipart")
    public static class MultipartProperties extends org.springframework.boot.autoconfigure.web.servlet.MultipartProperties {
    }
}
