package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "spring.data.elasticsearch")
public class ElasticsearchProperties {

    private String reindex;

    private ExecutorProperties reindexExecutor;

    private Duration reindexFrom;

    private ExecutorProperties executors;

    private boolean drop;

    private int port;

    private int searchPort;

    private String url;

    private Map<String, String> index;

    private boolean analyzerEnabled = false;

    private Map<String, Object> indexSettings = new HashMap<>();

    private Map<String, Object> mappingSettings = new HashMap<>();

    private List<String> defaultFilters = new ArrayList<>();

    private List<String> defaultSearchFilters = new ArrayList<>();

    @PostConstruct
    public void init() {
        indexSettings.putIfAbsent("max_result_window", 10000000);

        mappingSettings.putIfAbsent("date_detection", false);

        if (analyzerEnabled) {
            if (defaultFilters.isEmpty()) {
                defaultFilters.addAll(List.of("lowercase", "asciifolding", "keyword_repeat", "unique"));
            }
            if (defaultSearchFilters.isEmpty()) {
                defaultSearchFilters.addAll(List.of("lowercase", "asciifolding", "unique"));
            }
        }
    }
}
