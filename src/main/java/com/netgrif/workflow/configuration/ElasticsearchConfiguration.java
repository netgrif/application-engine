package com.netgrif.workflow.configuration;

import com.netgrif.workflow.workflow.service.CaseEventHandler;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class ElasticsearchConfiguration {

    @Value("${spring.data.elasticsearch.cluster-name:elasticsearch}")
    private String clusterName;

    @Value("${spring.data.elasticsearch.url:127.0.0.1}")
    private String url;

    @Value("${spring.data.elasticsearch.port:9300}")
    private int port;

    @Value("${spring.data.elasticsearch.index.case}")
    private String caseIndex;

    @Value("${spring.data.elasticsearch.index.task}")
    private String taskIndex;

    @Bean
    public String elasticCaseIndex() {
        return caseIndex;
    }

    @Bean
    public String elasticTaskIndex() {
        return taskIndex;
    }

    @Bean
    public Client client() throws UnknownHostException {
        Settings elasticsearchSettings = Settings.builder()
                .put("client.transport.sniff", true)
                .put("cluster.name", clusterName).build();
        TransportClient client = new PreBuiltTransportClient(elasticsearchSettings);
        client.addTransportAddress(new TransportAddress(InetAddress.getByName(url), port));
        return client;
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() throws UnknownHostException {
        return new ElasticsearchTemplate(client());
    }

    @Bean
    public CaseEventHandler caseEventHandler() {
        return new CaseEventHandler();
    }
}