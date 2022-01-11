package com.netgrif.workflow.configuration;

import com.netgrif.workflow.workflow.service.CaseEventHandler;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

@Configuration
public class ElasticsearchConfiguration {

    @Value("${spring.data.elasticsearch.url}")
    private String url;

    @Value("${spring.data.elasticsearch.searchport}")
    private int port;

    @Value("${spring.data.elasticsearch.index.case}")
    private String caseIndex;

    @Value("${spring.data.elasticsearch.index.task}")
    private String taskIndex;

    @Value("${spring.data.elasticsearch.reindex}")
    private String cron;

    @Bean
    public String springElasticsearchReindex() {
        return cron;
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
    public RestHighLevelClient client() {

        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(url, port, "http")));
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