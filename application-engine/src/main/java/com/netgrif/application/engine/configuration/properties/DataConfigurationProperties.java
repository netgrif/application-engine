package com.netgrif.application.engine.configuration.properties;

import co.elastic.clients.elasticsearch._types.Refresh;
import com.mongodb.connection.ClusterConnectionMode;
import com.mongodb.connection.ClusterType;
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
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import jakarta.validation.Valid;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
        String namespace = redis.getSession().getNamespace();
        if (namespace == null || namespace.isBlank() || "spring:session".equals(namespace)) {
            redis.getSession().setNamespace("spring:session:" + databaseName);
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


    @Data
    @NoArgsConstructor
    public static class Proxy {

        /**
         * Represents the hostname or IP address for the proxy server.
         */
        private String host;

        /**
         * Represents the port number for the proxy server.
         */
        private Integer port;

        /**
         * Represents the username associated with authentication for the proxy server.
         */
        @ToString.Exclude
        private String username;

        /**
         * Represents the password associated with authentication for the proxy server.
         */
        @ToString.Exclude
        private String password;
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
         * The maximum number of connections allowed. This value determines
         * the upper limit of concurrent connections that can be established
         * with the MongoDB server.
         */
        private int maxConnections = 100;

        /**
         * The minimum number of connections that should be maintained in the connection pool.
         * This value represents the baseline of connections that are kept ready for use,
         * even when demand for connections is low or idle.
         */
        private int minConnections = 0;

        /**
         * Specifies the maximum number of connections that can be initiated concurrently.
         * This property is used to throttle the number of simultaneous connection attempts
         * to limit resource usage and prevent connection saturation.
         */
        private int maxConnecting = 2;

        /**
         * Specifies the maximum amount of time
         * a connection can remain idle
         * before being eligible for closure or reallocation by the connection pool.
         * Use {@code connection-idle-time-unit } to change Time unit
         */
        private long connectionIdleTime;

        /**
         * Specifies the unit of time used to define the idle time for a connection.
         * This variable works in conjunction with {@code connection-idle-time} to configure
         * how long a connection can remain idle before being closed.
         */
        private TimeUnit connectionIdleTimeUnit = TimeUnit.MILLISECONDS;

        /**
         * Specifies the maximum amount of time that the application
         * will wait for a MongoDB connection acquisition from the connection pool
         * before a timeout is thrown. This is especially useful in scenarios with high
         * demand for connections or constrained resources.
         * Use {@code max-wait-time-unit} to set Time unit.
         */
        private long maxWaitTime = 120_000;

        /**
         * Specifies the time unit for the maximum waiting time for MongoDB operations.
         * This defines the unit of measurement, such as milliseconds, seconds, or minutes,
         * used in conjunction with the {@code max-wait-time} configuration.
         */
        private TimeUnit maxWaitTimeUnit = TimeUnit.MILLISECONDS;

        /**
         * Specifies the timeout for socket read and write operations.
         * This value determines the maximum period of inactivity before a socket operation is considered failed.
         * Use {@code socket-timeout-unit} to set Time unit.
         */
        private int socketTimeout = 10000;

        /**
         * Defines the time unit {@link TimeUnit} used for socket timeout configuration.
         * This property helps specify the granularity of the socket timeout value,
         * such as milliseconds, seconds, etc.
         */
        private TimeUnit socketTimeoutUnit = TimeUnit.MILLISECONDS;

        /**
         * The read timeout for MongoDB connections. If a read
         * operation does not complete within this timeframe, it will result in a
         * timeout exception.
         */
        private int readTimeout;

        /**
         * Specifies the unit of time for the read timeout.
         * The read timeout defines the maximum amount of time a read operation
         * can block before timing out. This variable uses {@link TimeUnit} to
         * represent the time unit (e.g., milliseconds, seconds).
         */
        private TimeUnit readTimeoutUnit = TimeUnit.MILLISECONDS;

        /**
         * Indicates whether SSL is enabled for the MongoDB connection.
         */
        private boolean ssl = false;

        /**
         * Defines the local threshold in milliseconds for MongoDB operations.
         * This value represents the acceptable latency difference between the primary
         * and a secondary server for read operations.
         */
        private long localThreshold = 15;

        /**
         * Specifies the time unit for the local threshold, which determines the acceptable
         * delay between the fastest and a candidate server in the database cluster.
         */
        private TimeUnit localThresholdUnit = TimeUnit.MILLISECONDS;

        /**
         * The mode variable specifies the cluster connection mode for MongoDB.
         * It defines how the application connects to a MongoDB cluster, such as single-node {@code SINGLE}
         * or multi-node {@code MULTIPLE} or {@code LOAD_BALANCED} configurations.
         */
        private ClusterConnectionMode mode;

        /**
         * The name of the MongoDB replica set to which the application should connect.
         * This property is used to specify the replica set name when working with
         * MongoDB clusters configured for high availability using replica sets.
         */
        private String replicaSetName;

        /**
         * Represents the type of the cluster used for MongoDB configuration.
         * Used to define the MongoDB cluster topology, such as whether
         * it is a replica set, sharded, or a standalone deployment.
         * Initialized to {@code ClusterType.UNKNOWN} by default.
         */
        private ClusterType clusterType = ClusterType.UNKNOWN;

        /**
         * Specifies the maximum amount of time, in milliseconds, that the client will wait
         * to select a server for an operation.
         */
        private long serverSelectionTimeout = 30000;

        /**
         * The {@code serverSelectionTimeoutUnit} variable represents the time unit for the server selection timeout.
         * This defines the time granularity (e.g., milliseconds, seconds) used when specifying the duration
         * of the server selection timeout for connections related to MongoDB properties.
         */
        private TimeUnit serverSelectionTimeoutUnit = TimeUnit.MILLISECONDS;

        /**
         * Represents the maximum length of a document log entry in the MongoProperties class.
         * This value specifies the upper limit for the size of individual document log entries.
         * Value must be greater than zero
         */
        private int maxDocumentLengthLog = 1000;

        /**
         * Indicates whether a proxy should be used for MongoDB connections.
         * This flag helps determine if the application will route its database communication
         * through a proxy server.
         */
        private boolean useProxy = false;

        /**
         * Represents a proxy configuration for MongoDB connections.
         * This variable allows specifying details such as host, port, username, and
         * password to connect to a MongoDB server through a proxy.
         */
        private Proxy proxy;

        /**
         * Proxy configuration for MongoDB communication. Formatted as String host:port.
         */
        private String proxyString;

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
         * Specifies the timeout duration, in milliseconds, for establishing a connection.
         * This configuration determines how long the system will wait before considering
         * the connection attempt as failed if no response is received within this time frame.
         */
        private long connectionTimeout = 1000;

        /**
         * Specifies the timeout value in milliseconds for socket connections.
         * This value determines how long the system waits for a response from
         * a socket connection before throwing a timeout exception.
         */
        private long socketTimeout = 30000;

        /**
         * Specifies the maximum number of total connections that can be established.
         */
        private int maxConnections = 30;

        /**
         * The maximum number of connections allowed per host by default.
         * This setting defines how many connections can be maintained simultaneously
         * to a single target endpoint or route, for example, a single server or service.
         */
        private int defaultMaxConnectionsPerHost = 10;

        /**
         * Represents the number of I/O threads to be used for handling input/output operations.
         * This configuration determines the level of concurrency for I/O tasks in the application.
         */
        private int ioThreadCount;

        /**
         * The time-to-live value for connections. Use {@code  connection-ttl-unit} to change unit of time.
         */
        private long connectionTtl;

        /**
         * Specifies the time unit for connection TTL (Time-To-Live) in the application.
         * This determines the granularity of the connection TTL setting, such as
         * seconds, milliseconds, or another valid {@link TimeUnit}.
         */
        private TimeUnit connectionTtlUnit;

        /**
         * Indicates whether the application should use a proxy for its Elasticsearch connections.
         * If set to {@code true}, the proxy will be enabled.
         */
        private boolean useProxy = false;

        /**
         * Proxy configuration for Elasticsearch communication. Formatted as String host:port.
         */
        private String proxyString;

        /**
         * Batch-related configuration properties for Elasticsearch operations.
         * These properties control the batch size for cases and tasks during
         * bulk operations to optimize performance and resource usage.
         */
        @Valid
        private BatchProperties batch = new BatchProperties();


        /**
         * Configuration properties for handling queues in Elasticsearch operations.
         * These properties specify the behavior of the ElasticQueueManager,
         * including the maximum queue size, delay between flush operations,
         * and the thread pool size for scheduled executor service tasks.
         */
        @Valid
        private QueueProperties queue = new QueueProperties();

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


        /**
         * Configuration properties for handling queues in Elasticsearch operations.
         * These properties specify the behavior of the ElasticQueueManager,
         * including the maximum queue size, delay between flush operations,
         * and the thread pool size for scheduled executor service tasks.
         */
        @Data
        public static class QueueProperties {

            /**
             * The size of the thread pool for the scheduled executor service.
             * This determines the number of threads available to schedule and execute tasks.
             * Default value: 10.
             */
            @Min(1)
            private int scheduledExecutorPoolSize = 10;

            /**
             * Delay time between flush operations in the queue.
             * This value represents the amount of time the scheduler waits before initiating the next flush.
             * Default value: 150.
             */
            @Min(1)
            private int delay = 150;

            /**
             * The time unit of the delay for flush operations.
             * Default value: {@link TimeUnit#MILLISECONDS}.
             */
            private TimeUnit delayUnit = TimeUnit.MILLISECONDS;

            /**
             * Maximum number of elements allowed in batch to flush.
             * When the batch size reaches this limit, it triggers a flush operation.
             * Default value: 400.
             */
            @Min(1)
            private int maxBatchSize = 400;


            /**
             * Specifies the maximum size of the queue. When the queue reaches this size,
             * a flush operation is triggered to process the elements in the queue.
             * Default value is 3000, and the minimum allowable value is 400.
             */
            @Min(400)
            private int maxQueueSize = 3000;

            /**
             * Defines the refresh policy for Elasticsearch operations.
             * Determines when changes made by bulk operations will be visible for search.
             * Default value is {@link RefreshPolicy#NONE}, meaning no immediate refresh.
             */
            private Refresh refreshPolicy = Refresh.False;
        }
    }


    /**
     * Represents configuration properties for Redis used within the application.
     * This class contains configurations related to the Redis server, including its
     * connection details, sentinel and cluster settings, and session management properties.
     */
    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.data.redis")
    public static class RedisProperties {

        /**
         * Hostname or IP address of the Redis server.
         * Default value is {@code "localhost"}.
         */
        private String host = "localhost";

        /**
         * Port number for connecting to the Redis server.
         * Default value is {@code 6379}.
         */
        private int port = RedisNode.DEFAULT_PORT;

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
         * Indicates whether SSL (Secure Sockets Layer) is enabled for connections.
         * Set to {@code true} to enable SSL or {@code false} to disable it.
         * This property is primarily used for configuring secure communication
         * with a Redis server.
         */
        private boolean ssl = false;

        /**
         * Configuration properties for Redis Sentinel.
         * <p>
         * This property defines the settings required for connecting to a Redis Sentinel
         * setup. It includes information about the master node, a list of sentinel nodes,
         * and optional authentication credentials such as username and password.
         */
        private RedisSentinelProperties sentinel = new RedisSentinelProperties();

        /**
         * Configuration property for managing Redis-based session settings for this application.
         * Uses the {@link EngineRedisSessionProperties} class to define specific session handling configurations.
         * Allows customization of session behavior such as session limiting and filtering.
         */
        private EngineRedisSessionProperties session = new EngineRedisSessionProperties();

        /**
         * Represents configuration properties for Redis Sentinel.
         * This class is typically used to configure and connect to a Redis Sentinel setup
         * by specifying the master node and the sentinel nodes involved.
         */
        @Data
        public static class RedisSentinelProperties {

            public static final String DEFAULT_SENTINEL_NODE = "localhost:" + RedisNode.DEFAULT_SENTINEL_PORT;

            /**
             * The name of the Redis master node to which Redis Sentinel clients should connect.
             * Specifies the master node in a Redis Sentinel deployment that is responsible for
             * managing the data and serving read/write queries.
             * This variable is essential for identifying the Redis master node among the available
             * nodes in the Sentinel setup.
             */
            private String master;

            /**
             * A list of Redis Sentinel nodes used for connection.
             * Each node in the list should be in the format of "host:port".
             * By default, this list contains a single node pointing to "localhost:26379".
             * In a Redis Sentinel setup, multiple nodes can be specified to ensure high availability and fault tolerance.
             */
            private List<String> nodes = List.of(DEFAULT_SENTINEL_NODE);

            /**
             * The username used for authentications or configurations related to Redis Sentinel properties.
             * This variable can be used to specify an optional username for connecting to a Redis database
             * when authentication is configured to require one.
             */
            private String username;

            /**
             * The password used for authentication with the Redis Sentinel setup.
             * This variable specifies the password needed to connect to the Redis database
             * when the configuration requires authentication for access.
             * It ensures secure communication and prevents unauthorized access to the database.
             */
            private String password;
        }

        /**
         * Configuration properties for Redis session management in the application.
         * <p>
         * This class extends {@link RedisSessionProperties}, providing additional
         * configurations specific to Redis-based session handling in the application.
         * It allows session limiting and other Redis-specific session settings.
         */
        @Data
        @EqualsAndHashCode(callSuper = true)
        public static class EngineRedisSessionProperties extends RedisSessionProperties {

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
