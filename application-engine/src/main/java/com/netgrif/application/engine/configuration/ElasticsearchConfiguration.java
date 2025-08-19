    package com.netgrif.application.engine.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.objects.elastic.serializer.LocalDateTimeJsonDeserializer;
import com.netgrif.application.engine.objects.elastic.serializer.LocalDateTimeJsonSerializer;
import com.netgrif.application.engine.workflow.service.CaseEventHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.*;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.elasticsearch.support.HttpHeaders;

import java.time.LocalDateTime;
import java.util.List;

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
        return clientBuilder.build();
    }

    @Bean(name = "elasticCaseObjectMapper")
    public ObjectMapper configureMapper() {
        ObjectMapper mapper = new ObjectMapper();

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeJsonSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeJsonDeserializer());

        mapper.registerModule(javaTimeModule);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
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
