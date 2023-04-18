package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.data.mongodb")
public class MongoProperties {

    private String uri;

    private String host;

    private String port;

    private String database;

    private boolean drop;
}
