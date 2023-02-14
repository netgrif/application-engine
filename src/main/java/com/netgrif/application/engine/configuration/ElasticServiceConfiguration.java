package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.ElasticsearchProperties;
import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository;
import com.netgrif.application.engine.elastic.domain.ElasticTaskRepository;
import com.netgrif.application.engine.elastic.service.ElasticCaseService;
import com.netgrif.application.engine.elastic.service.ElasticTaskService;
import com.netgrif.application.engine.elastic.service.executors.Executor;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

@Configuration
@ConditionalOnProperty(
        value = "nae.elastic.service.configuration.enable",
        matchIfMissing = true,
        havingValue = "true"
)
public class ElasticServiceConfiguration {

    @Autowired
    private ElasticCaseRepository caseRepository;

    @Autowired
    private ElasticTaskRepository taskRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

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
        return new ElasticCaseService(caseRepository, elasticsearchTemplate, executor());
    }

    @Bean
    @Primary
    public IElasticTaskService elasticTaskService() {
        return new ElasticTaskService(taskRepository, elasticsearchTemplate);
    }

    @Bean
    public IElasticCaseService reindexingTaskElasticCaseService() {
        return new ElasticCaseService(caseRepository, elasticsearchTemplate, reindexingTaskCaseExecutor());
    }


    @Bean
    public IElasticTaskService reindexingTaskElasticTaskService() {
        return new ElasticTaskService(taskRepository, elasticsearchTemplate);
    }

}
