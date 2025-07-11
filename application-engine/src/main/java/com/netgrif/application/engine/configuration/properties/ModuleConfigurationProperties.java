package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "netgrif.engine.module")
public class ModuleConfigurationProperties {

    private ServiceProperties service = new ServiceProperties();

    @Data
    public static class ServiceProperties {
        private boolean enabled = true;
    }
}
