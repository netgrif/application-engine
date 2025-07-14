package com.netgrif.application.engine.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class NetgrifEnvironmentPostProcessor implements EnvironmentPostProcessor  {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> override = new HashMap<>();

        putProperty(environment, override, "management.health.ldap.enabled", "netgrif.engine.management.health.ldap.enabled");
        putProperty(environment, override, "management.health.mail.enabled", "netgrif.engine.management.health.mail.enabled");
        putProperty(environment, override, "spring.main.allow-bean-definition-overriding", "netgrif.engine.main.allow-bean-definition-overriding");
        putProperty(environment, override, "spring.main.allow-circular-references", "netgrif.engine.main.allow-circular-references");
        putProperty(environment, override, "logging.config", "netgrif.engine.logging.config");
        putProperty(environment, override, "logging.file.path", "netgrif.engine.logging.file.path");
        putProperty(environment, override, "spring.autoconfigure.exclude", "netgrif.engine.main.autoconfigure-exclude");

        environment.getPropertySources().addFirst(new MapPropertySource("netgrifEngineProperties", override));
    }

    protected void putProperty(ConfigurableEnvironment environment, Map<String, Object> override, String originalPropertyame, String customPropertyName) {
        String property = environment.getProperty(customPropertyName);
        if (property != null) {
            override.put(originalPropertyame, property);
        }
    }
}
