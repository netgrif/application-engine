package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.ElasticsearchProperties;
import com.netgrif.application.engine.configuration.properties.UriProperties;
import com.netgrif.application.engine.workflow.service.CaseEventHandler;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

@Configuration
public class ElasticsearchConfiguration {

    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

    private final UriProperties uriProperties;

    public ElasticsearchConfiguration(UriProperties uriProperties) {
        this.uriProperties = uriProperties;
    }

    @Bean
    public String springElasticsearchReindex() {
        return elasticsearchProperties.getReindex();
    }

    @Bean
    public String elasticCaseIndex() {
        return elasticsearchProperties.getIndex().get("case");
    }

    @Bean
    public String elasticTaskIndex() {
        return elasticsearchProperties.getIndex().get("task");
    }

    @Bean
    public String elasticUriIndex() {
        return uriProperties.getIndex();
    }

    @Bean
    public RestHighLevelClient client() {

        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(elasticsearchProperties.getUrl(), elasticsearchProperties.getSearchPort(), "http")));
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(client());
    }

    @Bean
    public CaseEventHandler caseEventHandler() {
        return new CaseEventHandler();
    }
}
