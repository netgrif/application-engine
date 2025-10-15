package com.netgrif.application.engine.configuration;

import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.objects.elastic.serializer.LocalDateTimeJsonDeserializer;
import com.netgrif.application.engine.objects.elastic.serializer.LocalDateTimeJsonSerializer;
import com.netgrif.application.engine.workflow.service.CaseEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.reactor.IOReactorException;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.elasticsearch.RestClientBuilderCustomizer;
import org.springframework.context.annotation.*;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.elasticsearch.support.HttpHeaders;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static org.apache.http.impl.nio.reactor.IOReactorConfig.Builder.getDefaultMaxIoThreadCount;
import static org.elasticsearch.client.RestClientBuilder.*;

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

        if (elasticsearchProperties.isUseProxy()){
            clientBuilder.withProxy(elasticsearchProperties.getProxy());
        }

        try {
            NHttpClientConnectionManager connectionManager = getConnectionManager();
            clientBuilder.withClientConfigurer(ElasticsearchClients.ElasticsearchHttpClientConfigurationCallback.from(httpAsyncClientBuilder ->
                    httpAsyncClientBuilder.setConnectionManager(connectionManager)
                            // these values are validated in PoolEntry PoolingNHttpClientConnectionManager respectively
                            .setConnectionTimeToLive(elasticsearchProperties.getConnectionTtl(), elasticsearchProperties.getConnectionTtlUnit())));
        } catch (IOReactorException e) {
            log.error("Failed create connection manager", e);
            log.error("Continuing with default configuration...");
        }

        long connectionTimeout = elasticsearchProperties.getConnectionTimeout();
        long socketTimeout = elasticsearchProperties.getSocketTimeout();

        if (connectionTimeout <= 0) {
            connectionTimeout = DEFAULT_CONNECT_TIMEOUT_MILLIS;
        }

        if (socketTimeout <= 0) {
            socketTimeout = DEFAULT_SOCKET_TIMEOUT_MILLIS;
        }

        return clientBuilder
                .withConnectTimeout(Duration.ofMillis(connectionTimeout))
                .withSocketTimeout(Duration.ofMillis(socketTimeout))
                .build();
    }

    protected NHttpClientConnectionManager getConnectionManager() throws IOReactorException {
        int threadCount = elasticsearchProperties.getIoThreadCount();
        int totalConnections = elasticsearchProperties.getMaxConnections();
        int defaultMaxConnectionPerRoute = elasticsearchProperties.getDefaultMaxConnectionsPerHost();

        if (threadCount <= 0) {
            threadCount = getDefaultMaxIoThreadCount();
        }
        if (totalConnections <= 0) {
            totalConnections = DEFAULT_MAX_CONN_TOTAL;
        }
        if (defaultMaxConnectionPerRoute <= 0) {
            defaultMaxConnectionPerRoute = DEFAULT_MAX_CONN_PER_ROUTE;
        }

        IOReactorConfig config = IOReactorConfig.custom() // timeouts will be overridden by ClientConfiguration
                .setIoThreadCount(threadCount)
                .build();
        DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(config);
        PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor);
        connectionManager.setMaxTotal(totalConnections);
        connectionManager.setDefaultMaxPerRoute(defaultMaxConnectionPerRoute);
        return connectionManager;
    }

    @NotNull
    @Override
    public JsonpMapper jsonpMapper() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeJsonSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeJsonDeserializer());
        mapper.registerModule(javaTimeModule);
        return new JacksonJsonpMapper(mapper);
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
