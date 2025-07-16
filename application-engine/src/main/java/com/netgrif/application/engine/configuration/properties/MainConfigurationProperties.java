package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for the Netgrif engine's main application settings.
 * These properties allow customization of specific behaviors such as bean overriding,
 * circular reference handling, and exclusions for auto-configuration.
 */
@Data
@ConfigurationProperties(prefix = "netgrif.engine.main")
public class MainConfigurationProperties {

    /**
     * Indicates whether to allow overriding of bean definitions.
     * Default value is {@code false}.
     */
    private boolean allowBeanDefinitionOverriding = false;

    /**
     * Indicates whether to allow circular references between beans.
     * Default value is {@code false}.
     */
    private boolean allowCircularReferences = false;

    /**
     * A list of classes to be excluded from auto-configuration.
     * This list is empty by default.
     */
    private List<Class<?>> autoConfigureExclude = new ArrayList<>();
}
