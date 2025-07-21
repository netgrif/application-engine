package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for email settings within the Netgrif Application Engine.
 * Extends Spring Boot's {@link MailProperties} for additional customization.
 */
@Data
@Component
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "netgrif.engine.mail")
public class MailConfigurationProperties extends MailProperties {

    /**
     * The email address used as the sender's address in outgoing emails.
     * Default value: {@code test@example.com}.
     */
    private String mailFrom = "test@example.com";

    /**
     * Configuration properties for redirecting when testing email connections.
     */
    private RedirectToProperties redirectTo = new RedirectToProperties();

    /**
     * Toggles whether the mail server connection should be tested during application startup.
     * Default value: {@code false}.
     */
    private boolean testConnection = false;

    /**
     * Properties to define the redirect behavior for testing email connections.
     */
    @Data
    public static class RedirectToProperties {

        /**
         * The domain to which the email will be redirected during tests.
         * Default value: {@code localhost}.
         */
        private String domain = "localhost";

        /**
         * The port used for redirection during email testing.
         * Default value: {@code 4200}.
         */
        private String port = "4200";

        /**
         * Indicates whether SSL should be enabled for the connection during testing.
         * Default value: {@code false}.
         */
        private boolean ssl = false;
    }
}
