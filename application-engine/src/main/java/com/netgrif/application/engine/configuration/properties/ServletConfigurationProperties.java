package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "netgrif.engine.servlet")
public class ServletConfigurationProperties {
    private MultipartProperties multipart = new MultipartProperties();

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class MultipartProperties extends org.springframework.boot.autoconfigure.web.servlet.MultipartProperties {
    }
}
