package com.netgrif.application.engine.configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.netgrif.application.engine.configuration.properties.UriProperties;
import com.netgrif.application.engine.workflow.service.CaseEventHandler;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;

@Configuration
public class ElasticsearchConfiguration extends org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration {

    @Value("${spring.data.elasticsearch.url}")
    private String url;

    @Value("${spring.data.elasticsearch.searchport}")
    private int port;

    @Value("${spring.data.elasticsearch.index.petriNet}")
    private String petriNetIndex;

    @Value("${spring.data.elasticsearch.index.case}")
    private String caseIndex;

    @Value("${spring.data.elasticsearch.index.task}")
    private String taskIndex;

    @Value("${spring.data.elasticsearch.reindex}")
    private String cron;

    private final UriProperties uriProperties;

    public ElasticsearchConfiguration(UriProperties uriProperties) {
        this.uriProperties = uriProperties;
    }

    @Bean
    public String springElasticsearchReindex() {
        return cron;
    }

    @Bean
    public String elasticPetriNetIndex() {
        return petriNetIndex;
    }

    @Bean
    public String elasticCaseIndex() {
        return caseIndex;
    }

    @Bean
    public String elasticTaskIndex() {
        return taskIndex;
    }

    @Bean
    public String elasticUriIndex() {
        return uriProperties.getIndex();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient elasticSearchRestClient) {
        ElasticsearchTransport transport = new RestClientTransport(
                elasticSearchRestClient,
                new JacksonJsonpMapper()
        );
        return new ElasticsearchClient(transport);
    }

    @Bean
    public ElasticsearchTemplate elasticsearchTemplate(ElasticsearchClient elasticsearchClient) {
        return new ElasticsearchTemplate(elasticsearchClient);
    }

    @Bean
    public CaseEventHandler caseEventHandler() {
        return new CaseEventHandler();
    }

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(url + ":" + port)
                .build();
    }
}
