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

        putProperty(environment, override, "spring.main.allow-bean-definition-overriding", "netgrif.engine.main.allow-bean-definition-overriding");
        putProperty(environment, override, "spring.main.allow-circular-references", "netgrif.engine.main.allow-circular-references");
        putProperty(environment, override, "spring.autoconfigure.exclude", "netgrif.engine.main.autoconfigure-exclude");

        replacePrefix(environment, override, "logging", "netgrif.engine.logging");
        replacePrefix(environment, override, "management", "netgrif.engine.management");
        replacePrefix(environment, override, "spring.servlet.multipart", "netgrif.engine.servlet.multipart");

        environment.getPropertySources().addFirst(new MapPropertySource("netgrifEngineProperties", override));
    }

    protected void putProperty(ConfigurableEnvironment environment, Map<String, Object> override, String originalPropertyName, String customPropertyName) {
        String property = environment.getProperty(customPropertyName);
        if (property != null) {
            override.put(originalPropertyName, property);
        }
    }

    protected void replacePrefix(ConfigurableEnvironment environment, Map<String, Object> override, String originalPrefix, String customPrefix) {
        environment.getPropertySources().stream().forEach(propertySource -> {
            if (propertySource instanceof MapPropertySource) {
                Map<String, Object> map = ((MapPropertySource) propertySource).getSource();
                map.keySet().stream()
                        .filter(key -> key.startsWith(customPrefix))
                        .forEach(key -> override.put(originalPrefix + key.substring(customPrefix.length()), map.get(key)));
            }
        });
    }

}
