package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.ElasticsearchProperties;
import com.netgrif.application.engine.elastic.domain.repoitories.ElasticCaseRepository;
import com.netgrif.application.engine.elastic.service.*;
import com.netgrif.application.engine.elastic.service.executors.Executor;
import com.netgrif.application.engine.elastic.service.interfaces.*;
import com.netgrif.application.engine.elastic.service.query.ElasticPermissionQueryBuilder;
import com.netgrif.application.engine.elastic.service.query.ElasticTaskQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
    private ElasticsearchProperties elasticsearchProperties;

    @Autowired
    private IElasticCaseSearchService caseSearchService;

    @Autowired
    private IElasticTaskSearchService taskSearchService;

    @Autowired
    private ElasticPermissionQueryBuilder permissionQueryBuilder;

    @Autowired
    private IElasticCaseMappingService caseMappingService;

    @Autowired
    private ElasticTaskQueueManager elasticTaskQueueManager;

    @Autowired
    private ElasticTaskQueryBuilder taskQueryBuilder;

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
        return new ElasticCaseService(caseRepository, executor(), caseMappingService, caseSearchService, permissionQueryBuilder);
    }

    @Bean
    @Primary
    public IElasticTaskService elasticTaskService() {
        return new ElasticTaskService(taskSearchService, elasticTaskQueueManager, taskQueryBuilder, permissionQueryBuilder);
    }

    @Bean
    public IElasticCaseService reindexingTaskElasticCaseService() {
        return new ElasticCaseService(caseRepository, reindexingTaskCaseExecutor(), caseMappingService, caseSearchService,
                permissionQueryBuilder);
    }


    @Bean
    public IElasticTaskService reindexingTaskElasticTaskService() {
        return new ElasticTaskService(taskSearchService, elasticTaskQueueManager, taskQueryBuilder, permissionQueryBuilder);
    }

}
