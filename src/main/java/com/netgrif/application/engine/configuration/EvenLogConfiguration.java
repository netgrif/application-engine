package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.EventLogProperties;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@RequiredArgsConstructor
@EnableElasticsearchRepositories(
        basePackages = "com.netgrif.application.engine.history",
        elasticsearchTemplateRef = "eventLogElasticsearchTemplate"
)
public class EvenLogConfiguration {

    private final EventLogProperties eventLogProperties;

    @Bean
    public String elasticEventlogIndex() {
        return eventLogProperties.getElasticsearch().getIndex();
    }

    @Bean
    public RestHighLevelClient eventLogClient() {
        ClientConfiguration.MaybeSecureClientConfigurationBuilder config = ClientConfiguration.builder()
                .connectedTo(eventLogProperties.getElasticsearch().getUrl());
        if (eventLogProperties.getElasticsearch().getUrl().contains("https")) {
            ClientConfiguration.TerminalClientConfigurationBuilder secureConfig = config.usingSsl();
            if (eventLogProperties.getElasticsearch().hasCredentials()) {
                secureConfig = setBasicAuth(config);
            }
            return RestClients.create(secureConfig.build()).rest();
        }

        if (eventLogProperties.getElasticsearch().hasCredentials()) {
            config = setBasicAuth(config);
        }
        return RestClients.create(config.build()).rest();
    }

    @Bean
    public ElasticsearchOperations eventLogElasticsearchTemplate() {
        return new ElasticsearchRestTemplate(eventLogClient());
    }

    public <T extends ClientConfiguration.TerminalClientConfigurationBuilder> T setBasicAuth(T config) {
        return (T) config.withBasicAuth(eventLogProperties.getElasticsearch().getUsername(),
                eventLogProperties.getElasticsearch().getPassword());
    }


}
