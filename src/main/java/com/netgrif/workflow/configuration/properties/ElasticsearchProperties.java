package com.netgrif.workflow.configuration.properties;

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

    private int reindexSize;

    private Duration reindexFrom;

    private int executors;

    private boolean drop;

    private int port;

    private String url;

    private Map<String, String> index;
}