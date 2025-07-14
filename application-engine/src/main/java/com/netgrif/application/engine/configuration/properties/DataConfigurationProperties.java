package com.netgrif.application.engine.configuration.properties;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestProperties;
import org.springframework.boot.autoconfigure.session.RedisSessionProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

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
    private RedisProperties redis = new RedisProperties();
    private boolean drop = false;
    private String databaseName = "nae";
    private int imagePreviewScalingPx = 400;
    private ValidationProperties validation = new ValidationProperties();

    @Bean
    @Primary
    public MongoProperties mongoProperties() {
        if (mongodb.getDrop() == null) {
            mongodb.setDrop(drop);
        }
        if (mongodb.getDatabase() == null) {
            mongodb.setDatabase(databaseName);
        }
        return mongodb;
    }

    @Bean
    @Primary
    public ElasticsearchProperties elasticsearchProperties() {
        if (elasticsearch.getDrop() == null) {
            elasticsearch.setDrop(drop);
        }
        if (elasticsearch.getIndex() == null || elasticsearch.getIndex().isEmpty()) {
            elasticsearch.setIndex(Map.of(
                    ElasticsearchProperties.PETRI_NET_INDEX, databaseName + "_petrinet",
                    ElasticsearchProperties.CASE_INDEX, databaseName + "_case",
                    ElasticsearchProperties.TASK_INDEX, databaseName + "_task"
            ));
        }
        return elasticsearch;
    }

    @Bean
    @Primary
    public RedisProperties redisProperties() {
        if (redis.getNamespace() == null) {
            redis.setNamespace(databaseName);
        }
        return redis;
    }

    @Bean
    @Primary
    public RestProperties restProperties() {
        return rest;
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
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @ConfigurationProperties(prefix = "netgrif.engine.data.mongodb")
    public static class MongoProperties extends org.springframework.boot.autoconfigure.mongo.MongoProperties {
        private Boolean drop = false;
        private Boolean runnerEnsureIndex = true;
    }

    @Data
    @NoArgsConstructor
    @ConfigurationProperties(prefix = "netgrif.engine.data.elasticsearch")
    public static class ElasticsearchProperties {

        private String url = "localhost";

        private int port = 9300;

        private int searchPort = 9200;

        private String reindex;

        private ExecutorProperties reindexExecutor = new ExecutorProperties();

        private Duration reindexFrom;

        private ExecutorProperties executors = new ExecutorProperties();

        private Boolean drop;

        private Map<String, String> index;

        private boolean analyzerEnabled = false;

        private Resource analyzerPathFile;

        private Map<String, Object> indexSettings = new HashMap<>();

        private Map<String, Map<String, Object>> classSpecificIndexSettings = new HashMap<>();

        private Map<String, Object> mappingSettings = new HashMap<>();

        private List<String> defaultFilters = new ArrayList<>();

        private List<String> defaultSearchFilters = new ArrayList<>();

        private ServiceProperties service = new ServiceProperties();

        private PriorityProperties priority = new PriorityProperties();

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

        @Data
        public static class ServiceProperties {
            private boolean configurationEnabled = true;
            private boolean priority;
        }

        @Data
        public static class PriorityProperties {
            private List<String> fullTextFields = List.of(
                    "title.keyword^2",
                    "authorName^1",
                    "authorEmail^1",
                    "visualId.keyword^2"
            );

        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ConfigurationProperties(prefix = "netgrif.engine.session")
    public static class RedisProperties extends RedisSessionProperties {
        private String host = "localhost";
        private int port = 6379;
        private String username;
        private String password;
        private boolean enabledLimitSession = false;
        private int maxSession = 1;
        private boolean enabledFilter = false;
    }

    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.data.validation")
    public static class ValidationProperties {
        private boolean setDataEnabled = false;
    }
}
