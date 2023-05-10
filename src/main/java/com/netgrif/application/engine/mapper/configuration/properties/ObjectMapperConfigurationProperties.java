package com.netgrif.application.engine.mapper.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.jackson")
public class ObjectMapperConfigurationProperties {

    private Serialization serialization;

    private boolean defaultViewInclusion = true;

    private String mixinPackage = "com.netgrif.application.engine.mapper.mixins";
}
