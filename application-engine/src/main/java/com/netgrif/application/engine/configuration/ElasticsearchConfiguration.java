package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.workflow.service.CaseEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.TimeValue;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.*;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.elasticsearch.support.HttpHeaders;

import java.time.Duration;
import java.util.List;

import static co.elastic.clients.transport.rest5_client.low_level.Rest5ClientBuilder.DEFAULT_CONNECT_TIMEOUT_MILLIS;
import static co.elastic.clients.transport.rest5_client.low_level.Rest5ClientBuilder.DEFAULT_MAX_CONN_PER_ROUTE;
import static co.elastic.clients.transport.rest5_client.low_level.Rest5ClientBuilder.DEFAULT_MAX_CONN_TOTAL;
import static org.springframework.data.elasticsearch.client.elc.rest5_client.Rest5Clients.DEFAULT_SOCKET_TIMEOUT_MILLIS;
import static org.springframework.data.elasticsearch.client.elc.rest5_client.Rest5Clients.ElasticsearchConnectionManagerCallback;
import static org.springframework.data.elasticsearch.client.elc.rest5_client.Rest5Clients.ElasticsearchHttpClientConfigurationCallback;

@Slf4j
@Configuration
@EnableElasticsearchRepositories(excludeFilters = {
        @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.netgrif\\.application\\.engine\\.module\\..*"
        )
})
public class ElasticsearchConfiguration extends org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration {

    private final DataConfigurationProperties.ElasticsearchProperties elasticsearchProperties;

    public ElasticsearchConfiguration(DataConfigurationProperties.ElasticsearchProperties elasticsearchProperties) {
        this.elasticsearchProperties = elasticsearchProperties;
    }

    @Bean
    public String springElasticsearchReindex() {
        return elasticsearchProperties.getReindex();
    }

    @Bean
    public String elasticPetriNetIndex() {
        return elasticsearchProperties.getIndex().get(DataConfigurationProperties.ElasticsearchProperties.PETRI_NET_INDEX);
    }

    @Bean
    public String elasticCaseIndex() {
        return elasticsearchProperties.getIndex().get(DataConfigurationProperties.ElasticsearchProperties.CASE_INDEX);
    }

    @Bean
    public String elasticTaskIndex() {
        return elasticsearchProperties.getIndex().get(DataConfigurationProperties.ElasticsearchProperties.TASK_INDEX);
    }

    @Bean
    public CaseEventHandler caseEventHandler() {
        return new CaseEventHandler();
    }

    @NotNull
    @Override
    public ClientConfiguration clientConfiguration() {
        List<String> urls = sanitizeUrls(elasticsearchProperties.getUrl());

        ClientConfiguration.MaybeSecureClientConfigurationBuilder client = ClientConfiguration.builder()
                .connectedTo(urls.toArray(String[]::new));
        ClientConfiguration.TerminalClientConfigurationBuilder clientBuilder = client;

        if (elasticsearchProperties.isSsl()) {
            clientBuilder = client.usingSsl();
        }
        if (hasCredentials()) {
            clientBuilder = clientBuilder.withBasicAuth(elasticsearchProperties.getUsername(), elasticsearchProperties.getPassword());
        } else if (hasToken()) {
            clientBuilder.withHeaders(() -> {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + elasticsearchProperties.getToken());
                return headers;
            });
        }

        if (elasticsearchProperties.isUseProxy()) {
            String proxy = elasticsearchProperties.getProxyString();
            if (proxy != null && !proxy.isBlank()) {
                clientBuilder.withProxy(proxy);
            } else {
                log.warn("Elasticsearch proxy is enabled but proxyString is blank; ignoring proxy configuration.");
            }
        }

        clientBuilder.withClientConfigurer(ElasticsearchHttpClientConfigurationCallback.from(this::configureHttpAsyncClientBuilder))
                .withClientConfigurer(ElasticsearchConnectionManagerCallback.from(this::configureConnectionManager));

        long connectionTimeout = elasticsearchProperties.getConnectionTimeout();
        long socketTimeout = elasticsearchProperties.getSocketTimeout();

        if (connectionTimeout <= 0) {
            connectionTimeout = DEFAULT_CONNECT_TIMEOUT_MILLIS;
        }

        if (socketTimeout <= 0) {
            socketTimeout = DEFAULT_SOCKET_TIMEOUT_MILLIS;
        }

        clientBuilder.withConnectTimeout(Duration.ofMillis(connectionTimeout))
                .withSocketTimeout(Duration.ofMillis(socketTimeout));

        log.debug("ES HTTP client: ioThreads={}, maxTotal={}, maxPerRoute={}, connectTimeoutMs={}, socketTimeoutMs={}, ttl={} {}, proxy={}",
                elasticsearchProperties.getIoThreadCount(), elasticsearchProperties.getMaxConnections(), elasticsearchProperties.getDefaultMaxConnectionsPerHost(), connectionTimeout, socketTimeout,
                elasticsearchProperties.getConnectionTtl(), elasticsearchProperties.getConnectionTtlUnit(), elasticsearchProperties.isUseProxy());

        return clientBuilder.build();
    }

 /**   @NotNull
    @Override
    public JsonpMapper jsonpMapper() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeJsonSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeJsonDeserializer());
        mapper.registerModule(javaTimeModule);
        return new JacksonJsonpMapper(mapper);
    }*/

    protected HttpAsyncClientBuilder configureHttpAsyncClientBuilder(HttpAsyncClientBuilder httpAsyncClientBuilder) {
        int threadCount = elasticsearchProperties.getIoThreadCount();

        if (threadCount <= 0) {
            threadCount = IOReactorConfig.DEFAULT.getIoThreadCount();
        }

        IOReactorConfig config = IOReactorConfig.custom()
                .setIoThreadCount(threadCount)
                .build();

        return httpAsyncClientBuilder.setIOReactorConfig(config);
    }

    protected PoolingAsyncClientConnectionManagerBuilder configureConnectionManager(
            PoolingAsyncClientConnectionManagerBuilder connectionManagerBuilder) {
        int maxPerRoute = elasticsearchProperties.getDefaultMaxConnectionsPerHost();
        int maxTotal = elasticsearchProperties.getMaxConnections();

        connectionManagerBuilder
                .setMaxConnPerRoute(maxPerRoute > 0 ? maxPerRoute : DEFAULT_MAX_CONN_PER_ROUTE)
                .setMaxConnTotal(maxTotal > 0 ? maxTotal : DEFAULT_MAX_CONN_TOTAL);

        if (elasticsearchProperties.getConnectionTtl() > 0 && elasticsearchProperties.getConnectionTtlUnit() != null) {
            connectionManagerBuilder.setConnectionTimeToLive(TimeValue.of(
                    elasticsearchProperties.getConnectionTtl(),
                    elasticsearchProperties.getConnectionTtlUnit()));
        }

        return connectionManagerBuilder;
    }


    private boolean hasCredentials() {
        return elasticsearchProperties.getUsername() != null && !elasticsearchProperties.getUsername().isBlank() &&
                elasticsearchProperties.getPassword() != null && !elasticsearchProperties.getPassword().isBlank();
    }

    private boolean hasToken() {
        return elasticsearchProperties.getToken() != null && !elasticsearchProperties.getToken().isBlank();
    }

    private List<String> sanitizeUrls(List<String> urls) {
        return urls.stream().map(u -> u.contains(":") ? u : u + ":" + elasticsearchProperties.getSearchPort()).toList();
    }
}
