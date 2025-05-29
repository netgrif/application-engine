package com.netgrif.application.engine.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.layout.grid")
public class LayoutGridConfiguration {
    private final Map<String, String> root = new HashMap<>();
    private final Map<String, String> container = new HashMap<>();
    private final Map<String, String> children = new HashMap<>();
}
