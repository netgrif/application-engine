package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "netgrif.engine.main")
public class MainConfigurationProperties {

    private boolean allowBeanDefinitionOverriding = false;
    private boolean allowCircularReferences = false;
    private List<Class<?>> autoConfigureExclude = new ArrayList<>();
}
