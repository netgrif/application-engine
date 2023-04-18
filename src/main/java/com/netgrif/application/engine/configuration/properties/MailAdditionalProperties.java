package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.mail.properties.mail")
public class MailAdditionalProperties {

    private boolean debug;

    private SmtpProperties smtp;
}
