package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Data
@ConfigurationProperties(prefix = "netgrif.engine.servlet")
public class ServletConfigurationProperties {

    @Data
    @Primary
    @Component
    @EqualsAndHashCode(callSuper = true)
    @ConfigurationProperties(prefix = "netgrif.engine.servlet.multipart")
    public static class Multipart extends MultipartProperties {
    }
}
