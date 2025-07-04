    package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.workflow.service.CaseEventHandler;
import org.springframework.context.annotation.*;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

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

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchProperties.getUrl() + ":" + elasticsearchProperties.getSearchPort())
                .build();
    }
}
