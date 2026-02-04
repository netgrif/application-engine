package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository;
import com.netgrif.application.engine.elastic.service.interfaces.*;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnExpression("'${netgrif.engine.data.elasticsearch.reindex}'!= 'null'")
public class ReindexingTask {

    private static final Logger log = LoggerFactory.getLogger(ReindexingTask.class);

    private int pageSize;
    private TaskRepository taskRepository;
    private ElasticCaseRepository elasticCaseRepository;
    private IElasticCaseService elasticCaseService;
    private IElasticTaskService elasticTaskService;
    private IElasticCaseMappingService caseMappingService;
    private IElasticTaskMappingService taskMappingService;
    private IWorkflowService workflowService;
    private DataConfigurationProperties.ElasticsearchProperties elasticsearchProperties;
    private LocalDateTime lastRun;
    private IElasticIndexService elasticIndexService;

    @Autowired
    public ReindexingTask(
            TaskRepository taskRepository,
            ElasticCaseRepository elasticCaseRepository,
            @Qualifier("reindexingTaskElasticCaseService")
            IElasticCaseService elasticCaseService,
            @Qualifier("reindexingTaskElasticTaskService")
            IElasticTaskService elasticTaskService,
            IElasticCaseMappingService caseMappingService,
            IElasticTaskMappingService taskMappingService,
            IWorkflowService workflowService,
            DataConfigurationProperties.ElasticsearchProperties elasticsearchProperties,
            IElasticIndexService elasticIndexService) {
        this.taskRepository = taskRepository;
        this.elasticCaseRepository = elasticCaseRepository;
        this.elasticCaseService = elasticCaseService;
        this.elasticTaskService = elasticTaskService;
        this.caseMappingService = caseMappingService;
        this.taskMappingService = taskMappingService;
        this.workflowService = workflowService;
        this.elasticsearchProperties = elasticsearchProperties;
        this.pageSize = elasticsearchProperties.getReindexExecutor().getSize();
        this.elasticIndexService = elasticIndexService;

        lastRun = LocalDateTime.now();
        if (this.elasticsearchProperties.getReindexFrom() != null) {
            lastRun = lastRun.minus(this.elasticsearchProperties.getReindexFrom());
        }
    }

    @Scheduled(cron = "#{springElasticsearchReindex}")
    public void reindex() {
        log.info("Reindexing stale cases: started reindexing after {}", lastRun);

        elasticIndexService.bulkIndex(false, lastRun, null, null);

        log.info("Reindexing stale cases: end");
    }

    public void forceReindexPage(Predicate predicate, int page, long numOfPages) {
        reindexPage(predicate, page, numOfPages, true);
    }

    private void reindexPage(Predicate predicate, int page, long numOfPages, boolean forced) {
        log.info("Reindexing {} / {}", page + 1, numOfPages);
        Page<Case> cases = this.workflowService.search(predicate, PageRequest.of(page, pageSize));

        for (Case aCase : cases) {
            if (forced || elasticCaseRepository.countByIdAndLastModified(aCase.getStringId(), Timestamp.valueOf(aCase.getLastModified()).getTime()) == 0) {
                elasticCaseService.indexNow(this.caseMappingService.transform(aCase));
                List<Task> tasks = taskRepository.findAllByCaseId(aCase.getStringId());
                for (Task task : tasks) {
                    elasticTaskService.indexNow(this.taskMappingService.transform(task));
                }
            }
        }
    }
}
