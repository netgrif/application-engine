package com.netgrif.application.engine.history.domain;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(elasticsearchTemplateRef = "historyCluster")
public class EventLogRepositoryConfiguration {
}
