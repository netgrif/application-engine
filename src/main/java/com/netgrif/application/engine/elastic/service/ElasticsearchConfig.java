package com.netgrif.application.engine.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.netgrif.application.engine.configuration.properties.ElasticsearchProperties;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {
    private final ElasticsearchProperties elasticsearchProperties;

    ElasticsearchConfig(ElasticsearchProperties elasticsearchProperties) {
        this.elasticsearchProperties = elasticsearchProperties;
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = RestClient.builder(new HttpHost(elasticsearchProperties.getUrl(), elasticsearchProperties.getSearchPort())).build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new ElasticSearchJsonpMapper());
        return new ElasticsearchClient(transport);

    }
}
