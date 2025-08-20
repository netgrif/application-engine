package com.netgrif.application.engine.configuration.properties;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestProperties;
import org.springframework.boot.autoconfigure.session.RedisSessionProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import jakarta.validation.Valid;
import java.time.Duration;
import java.util.*;

/**
 * Configuration properties for the application engine's data functionality.
 * <p>
 * This class contains configurations specific to data management, particularly
 * related to REST API settings.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "netgrif.engine.data")
public class DataConfigurationProperties {

    /**
     * REST API configuration properties specific to the application engine.
     */
    private RestProperties rest = new RestProperties();
    
    /**
     * MongoDB configuration properties related to database connection and behavior.
     */
    private MongoProperties mongodb = new MongoProperties();
    
    /**
     * Elasticsearch properties for configuring search-related functionality.
     */
    private ElasticsearchProperties elasticsearch = new ElasticsearchProperties();
    
    /**
     * Redis configuration properties for caching and session management.
     */
    private RedisProperties redis = new RedisProperties();
    
    /**
     * Flag indicating whether to drop collections or indexes during application initialization.
     */
    private boolean drop = false;
    
    /**
     * Name of the primary database used by the application engine.
     */
    private String databaseName = "nae";
    
    /**
     * Size in pixels for scaling the image preview.
     */
    private int imagePreviewScalingPx = 400;
    
    /**
     * Validation-specific configuration properties.
     */
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

    /**
     * Configuration properties for the REST-specific settings under the
     * {@code netgrif.engine.data.rest} prefix.
     * <p>
     * This class extends {@link RepositoryRestProperties} to inherit
     * Spring Data REST functionalities while adding application-specific customizations.
     */
    @Data
    @Primary
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class RestProperties extends RepositoryRestProperties {
    }

    /**
     * MongoDB-specific configuration properties for the data module.
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class MongoProperties extends org.springframework.boot.autoconfigure.mongo.MongoProperties {
        
        /**
         * Flag indicating whether the database should drop collections during initialization.
         */
        private Boolean drop = false;

        /**
         * Ensures indexes are applied during initialization, if true.
         */
        private Boolean runnerEnsureIndex = true;

        /**
         * Multi-value map for MongoDB indexes.
         * <p>
         * This property holds a mapping between entity classes and their associated
         * collection of index definitions. Each entry in the map corresponds to a class
         * and its set of index configurations for MongoDB.
         */
        private MultiValueMap<Class<?>, String> indexes = new LinkedMultiValueMap<>();
    }

    /**
     * Elasticsearch-related configuration properties for indexing and searching.
     */
    @Data
    @NoArgsConstructor
    public static class ElasticsearchProperties {
        /**
         * Hostname for the Elasticsearch server.
         */
        private List<String> url = List.of("localhost");

        /**
         * Indicates if SSL is enabled for Elasticsearch communication.
         */
        private boolean ssl = false;

        /**
         * Port for connecting to Elasticsearch transport client.
         */
        private int port = 9300;

        /**
         * Port for accessing the Elasticsearch HTTP client.
         */
        private int searchPort = 9200;

        /**
         * The username used for authenticating with the Elasticsearch server.
         */
        @ToString.Exclude
        private String username = null;

        /**
         * The password used for authenticating with the Elasticsearch server.
         */
        @ToString.Exclude
        private String password = null;

        /**
         * The authentication token for the Elasticsearch server, when using token-based authentication.
         */
        @ToString.Exclude
        private String token = null;

        /**
         * Command to trigger a reindexing job.
         */
        private String reindex;

        /**
         * Reindexing executor properties for managing threads and timeouts.
         */
        private ExecutorProperties reindexExecutor = new ExecutorProperties();

        /**
         * Duration to control reindexing starting point.
         */
        private Duration reindexFrom;

        /**
         * Generic executor properties for Elasticsearch operations.
         */
        private ExecutorProperties executors = new ExecutorProperties();

        /**
         * Flag determining whether to drop indexes upon initialization.
         */
        private Boolean drop;

        /**
         * Map of index names by their associated types.
         */
        private Map<String, String> index;

        /**
         * Activates an analyzer for document indexing, if true.
         */
        private boolean analyzerEnabled = false;

        /**
         * File path for the analyzer configuration.
         */
        private Resource analyzerPathFile;

        /**
         * General index settings applied to all indexes.
         */
        private Map<String, Object> indexSettings = new HashMap<>();

        /**
         * Settings for specific classes, overriding general index options.
         */
        private Map<String, Map<String, Object>> classSpecificIndexSettings = new HashMap<>();

        /**
         * Mappings for document fields, such as data type detection rules.
         */
        private Map<String, Object> mappingSettings = new HashMap<>();

        /**
         * Default filters applied during document indexing.
         */
        private List<String> defaultFilters = new ArrayList<>();

        /**
         * Default filters applied during document searching.
         */
        private List<String> defaultSearchFilters = new ArrayList<>();

        /**
         * Services related to Elasticsearch functionalities.
         */
        private ServiceProperties service = new ServiceProperties();

        /**
         * Properties for configuring priority indexing and searches.
         */
        private PriorityProperties priority = new PriorityProperties();


        /**
         * Batch-related configuration properties for Elasticsearch operations.
         * These properties control the batch size for cases and tasks during
         * bulk operations to optimize performance and resource usage.
         */
        @Valid
        private BatchProperties batch = new BatchProperties();

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
            
            /**
             * Number of threads for executor service.
             */
            private int size = 50;

            /**
             * Timeout duration (in seconds) for executor service tasks.
             */
            private int timeout = 5;

            /**
             * Maximum number of threads allowed in the thread pool.
             * This value is calculated as {@code size * 10}.
             */
            private int maxPoolSize = size * 10;

            /**
             * Determines whether core threads are allowed to time out.
             * If {@code true}, core threads will be terminated when idle for
             * longer than {@link #keepAliveSeconds}.
             */
            private boolean allowCoreThreadTimeOut = true;

            /**
             * The maximum time (in seconds) that excess idle threads
             * will wait before being terminated.
             */
            private int keepAliveSeconds = 30;

            /**
             * A prefix used for naming threads in the executor.
             * Threads are named as {@code <threadNamePrefix>-<threadNumber>}.
             */
            private String threadNamePrefix = "ElasticTaskExecutor-";
        }

        @Data
        public static class ServiceProperties {
            
            /**
             * Enables Elasticsearch service configurations, if true.
             */
            private boolean configurationEnabled = true;

            /**
             * Toggle for prioritizing specific Elasticsearch features.
             */
            private boolean priority;
        }

        @Data
        public static class PriorityProperties {
            
            /**
             * List of fields with boosted priorities in full-text search operations.
             */
            private List<String> fullTextFields = List.of(
                    "title.keyword^2",
                    "authorName^1",
                    "authorEmail^1",
                    "visualId.keyword^2"
            );
        }

        /**
         * Configuration properties for batch operations in Elasticsearch.
         * This class specifies the batch sizes for cases and tasks when performing
         * bulk operations like indexing or updating. These values are used to
         * control and optimize resource consumption during high-load processes.
         */
        @Data
        public static class BatchProperties {

            /**
             * Default batch size for cases during Elasticsearch bulk operations.
             * This value must be at least 1. The default is 5000.
             */
            @Min(1)
            private int caseBatchSize = 5000;

            /**
             * Default batch size for tasks during Elasticsearch bulk operations.
             * This value must be at least 1. The default is 20000.
             */
            @Min(1)
            private int taskBatchSize = 20000;
        }
    }
    
    /**
     * Configuration properties for Redis session management in the application.
     * <p>
     * This class extends {@link RedisSessionProperties}, providing additional
     * configurations specific to Redis-based session handling in the application.
     * It allows customization of connection details, session limiting, and other Redis-specific settings.
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ConfigurationProperties(prefix = "netgrif.engine.session")
    public static class RedisProperties extends RedisSessionProperties {

        /**
         * Hostname or IP address of the Redis server.
         * Default value is {@code "localhost"}.
         */
        private String host = "localhost";

        /**
         * Port number for connecting to the Redis server.
         * Default value is {@code 6379}.
         */
        private int port = 6379;

        /**
         * Username for authenticating with the Redis server.
         * If null or empty, no username will be used.
         */
        private String username;

        /**
         * Password for authenticating with the Redis server.
         * If null or empty, no password will be used.
         */
        private String password;

        /**
         * Flag indicating whether to enable session limit functionality.
         * If {@code true}, sessions will be limited based on the configured {@link #maxSession} value.
         */
        private boolean enabledLimitSession = false;

        /**
         * Maximum number of sessions allowed per user when session limiting is enabled.
         * Default value is {@code 1}.
         */
        private int maxSession = 1;

        /**
         * Flag indicating whether Redis filtering is enabled.
         * Default value is {@code false}.
         */
        private boolean enabledFilter = false;
    }

    /**
     * Configuration properties for validation in the Netgrif Application Engine.
     * <p>
     * Allows configuration of validation-related behaviors, such as enabling or disabling
     * specific validation settings.
     */
    @Data
    public static class ValidationProperties {

        /**
         * Determines whether setting data is enabled.
         * If {@code true}, certain data modifications are allowed as part of validation operations.
         * The default value is {@code false}.
         */
        private boolean setDataEnabled = false;
    }
}
