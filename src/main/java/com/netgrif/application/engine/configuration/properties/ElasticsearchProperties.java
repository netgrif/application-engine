package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@ConfigurationProperties(prefix = "spring.data.elasticsearch")
@Data
@Component
public class ElasticsearchProperties {

    private String reindex;

    private int reindexSize = 20;

    private int reindexTimeout = 60;

    private Duration reindexFrom;

    private int executors = 500;

    private int executorsTimeout = 5;

    private boolean drop;

    private int port;

    private int searchPort;

    private String url;

    private Map<String, String> index;
}
