package com.netgrif.application.engine.configuration.properties;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for the application engine's data functionality.
 * <p>
 * This class contains configurations specific to data management, particularly
 * related to REST API settings.
 */
@Data
@ConfigurationProperties(prefix = "netgrif.engine.data")
public class DataConfigurationProperties {

    private RestProperties rest = new RestProperties();
    private MongoProperties mongodb = new MongoProperties();
    private ElasticsearchProperties elasticsearch = new ElasticsearchProperties();
    private boolean drop = false;
    private String databaseName = "nae";

    @PostConstruct
    void init() {
        if (mongodb.getDrop() == null) {
            mongodb.setDrop(drop);
        }
        if (mongodb.getDatabase() == null) {
            mongodb.setDatabase(databaseName);
        }
        if (elasticsearch.getDrop() == null) {
            elasticsearch.setDrop(drop);
        }
        if (elasticsearch.getIndex() == null || elasticsearch.getIndex().isEmpty()) {
            elasticsearch.setIndex(Map.of(
                    ElasticsearchProperties.PETRI_NET_INDEX, databaseName + "_petriNet",
                    ElasticsearchProperties.CASE_INDEX, databaseName + "_case",
                    ElasticsearchProperties.TASK_INDEX, databaseName + "_task"
            ));
        }
    }

    /**
     * Configuration properties for the REST-specific settings under the
     * {@code netgrif.engine.data.rest} prefix.
     * <p>
     * This class extends {@link RepositoryRestProperties} to inherit
     * Spring Data REST functionalities while adding application-specific customizations.
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @ConfigurationProperties(prefix = "netgrif.engine.data.rest")
    public static class RestProperties extends RepositoryRestProperties {
    }

    @Data
    @Primary
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @ConfigurationProperties(prefix = "netgrif.engine.data.mongodb")
    public static class MongoProperties extends org.springframework.boot.autoconfigure.mongo.MongoProperties {
        private Boolean drop = true;
        private Boolean runnerEnsureIndex = true;
    }

    @Data
    @NoArgsConstructor
    @ConfigurationProperties(prefix = "netgrif.engine.data.elasticsearch")
    public static class ElasticsearchProperties {

        private String url;

        private int port;

        private int searchPort;

        private String reindex;

        private ExecutorProperties reindexExecutor;

        private Duration reindexFrom;

        private ExecutorProperties executors;

        private Boolean drop;

        private Map<String, String> index;

        private boolean analyzerEnabled = false;

        private Resource analyzerPathFile;

        private Map<String, Object> indexSettings = new HashMap<>();

        private Map<String, Map<String, Object>> classSpecificIndexSettings = new HashMap<>();

        private Map<String, Object> mappingSettings = new HashMap<>();

        private List<String> defaultFilters = new ArrayList<>();

        private List<String> defaultSearchFilters = new ArrayList<>();

        public static final String PETRI_NET_INDEX = "petriNet";

        public static final String CASE_INDEX = "case";

        public static final String TASK_INDEX = "task";

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

        public Map<String, Object> getClassSpecificSettings(String className) {
            return classSpecificIndexSettings.getOrDefault(className, new HashMap<>());
        }

        @Data
        public static class ExecutorProperties {
            private int size = 500;
            private int timeout = 5;
        }
    }
}
