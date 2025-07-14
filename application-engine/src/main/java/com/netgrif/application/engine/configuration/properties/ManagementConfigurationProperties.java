package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.logging.LogFileWebEndpointProperties;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Data
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "netgrif.engine.management")
public class ManagementConfigurationProperties {
    private EndpointsProperties endpoints = new EndpointsProperties();
    private EndpointProperties endpoint = new EndpointProperties();
    private final BuildProperties info;

    @Data
    @Component
    @ConfigurationProperties(prefix = "netgrif.engine.management.endpoints")
    public static class EndpointsProperties {
        private WebEndpointProperties web = new WebEndpointProperties();
    }

    @Data
    @Component
    @ConfigurationProperties(prefix = "netgrif.engine.management.endpoint")
    public static class EndpointProperties {
        private HealthEndpointProperties health = new HealthEndpointProperties();
        private LogFileWebEndpointProperties logFile = new LogFileWebEndpointProperties();
    }

    @Data
    @Component
    @ConfigurationProperties(prefix = "netgrif.engine.management.health")
    public static class HealthProperties {
        private MailHealthProperties mail = new MailHealthProperties();
        private LdapHealthProperties ldap = new LdapHealthProperties();

        @Data
        public static class MailHealthProperties {
            private boolean enabled = false;
        }

        @Data
        public static class LdapHealthProperties {
            private boolean enabled = false;
        }
    }

}
