package com.netgrif.application.engine.configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository;
import com.netgrif.application.engine.elastic.domain.ElasticTaskRepository;
import com.netgrif.application.engine.elastic.service.ElasticCaseService;
import com.netgrif.application.engine.elastic.service.ElasticTaskService;
import com.netgrif.application.engine.elastic.service.executors.Executor;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCasePrioritySearch;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = "netgrif.engine.data.elasticsearch.service.configuration-enabled",
        matchIfMissing = true,
        havingValue = "true"
)
public class ElasticServiceConfiguration {

    private final ElasticCaseRepository caseRepository;
    private final ElasticsearchTemplate elasticsearchTemplate;
    private final DataConfigurationProperties.ElasticsearchProperties elasticsearchProperties;
    private final IPetriNetService petriNetService;
    private final IWorkflowService workflowService;
    private final IElasticCasePrioritySearch elasticCasePrioritySearch;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ElasticsearchClient elasticsearchClient;

    @Bean
    @Primary
    public Executor executor() {
        return new Executor(elasticsearchProperties.getExecutors().getSize(), elasticsearchProperties.getExecutors().getTimeout());
    }

    @Bean
    public Executor reindexingTaskCaseExecutor() {
        return new Executor(elasticsearchProperties.getReindexExecutor().getSize(), elasticsearchProperties.getReindexExecutor().getTimeout());
    }

    @Bean
    public Executor reindexingTaskTaskExecutor() {
        return new Executor(elasticsearchProperties.getReindexExecutor().getSize(), elasticsearchProperties.getReindexExecutor().getTimeout());
    }

    @Bean
    @Primary
    public IElasticCaseService elasticCaseService() {
        return new ElasticCaseService(
                caseRepository,
                elasticsearchTemplate,
                executor(),
                elasticsearchProperties,
                petriNetService,
                workflowService,
                elasticCasePrioritySearch,
                applicationEventPublisher,
                elasticsearchClient
        );
    }

    @Bean
    @Primary
    public IElasticTaskService elasticTaskService() {
        return new ElasticTaskService(elasticsearchTemplate);
    }

    @Bean
    public IElasticCaseService reindexingTaskElasticCaseService() {
        return new ElasticCaseService(
                caseRepository,
                elasticsearchTemplate,
                reindexingTaskCaseExecutor(),
                elasticsearchProperties,
                petriNetService,
                workflowService,
                elasticCasePrioritySearch,
                applicationEventPublisher,
                elasticsearchClient
        );
    }


    @Bean
    public IElasticTaskService reindexingTaskElasticTaskService() {
        return new ElasticTaskService(elasticsearchTemplate);
    }

}
