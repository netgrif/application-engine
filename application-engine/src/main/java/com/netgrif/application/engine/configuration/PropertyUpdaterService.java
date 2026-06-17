package com.netgrif.application.engine.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PropertyUpdaterService {
    private static final String DYNAMIC_PROPERTIES_SOURCE_NAME = "dynamicProperties";

    private final ConfigurableEnvironment environment;

    public void updateProperty(String key, String value) {
        MutablePropertySources propertySources = environment.getPropertySources();
        if (!propertySources.contains(DYNAMIC_PROPERTIES_SOURCE_NAME)) {
            Map<String, Object> dynamicProperties = new HashMap<>();
            dynamicProperties.put(key, value);
            propertySources.addFirst(new MapPropertySource(DYNAMIC_PROPERTIES_SOURCE_NAME, dynamicProperties));
        } else {
            MapPropertySource propertySource = (MapPropertySource) propertySources.get(DYNAMIC_PROPERTIES_SOURCE_NAME);
            Objects.requireNonNull(propertySource).getSource().put(key, value);
        }
    }
}
