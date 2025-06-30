package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "netgrif.engine.servlet")
public class ServletConfigurationProperties {
    private Multipart multipart = new Multipart();

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Multipart extends MultipartProperties {
    }
}
