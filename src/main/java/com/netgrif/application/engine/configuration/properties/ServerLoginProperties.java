package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nae.server.login")
public class ServerLoginProperties {

    private Integer attempts;

    private Integer timeout;

}
