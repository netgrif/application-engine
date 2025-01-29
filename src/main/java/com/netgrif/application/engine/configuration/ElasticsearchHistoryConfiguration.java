package com.netgrif.application.engine.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.client.ClientConfiguration;

public class ElasticsearchHistoryConfiguration extends org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration {

    @Value("${spring.data.elasticsearch.history.url}")
    private String url;

    @Value("${spring.data.elasticsearch.history.searchport}")
    private int port;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(url + ":" + port)
                .build();
    }
}
