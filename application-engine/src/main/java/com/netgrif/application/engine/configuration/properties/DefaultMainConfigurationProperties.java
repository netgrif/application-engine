package com.netgrif.application.engine.configuration.properties;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DefaultMainConfigurationProperties {

    public static final Map<String, Object> DEFAULT_MAIN_PROPERTIES = Map.of(
            "spring.main.allow-bean-definition-overriding", "true",
            "spring.main.allow-circular-references", "true",
            "logging.config", "classpath:logback.xml",
            "spring.autoconfigure.exclude", "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration");
}
