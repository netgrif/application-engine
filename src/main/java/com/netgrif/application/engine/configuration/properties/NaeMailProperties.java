package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "nae.mail")
public class NaeMailProperties {

    private String from;

    private MailRedirectProperties redirectTo;
}
