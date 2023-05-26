package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnExpression("'${spring.data.elasticsearch.reindex}'!= 'null'")
public class ReindexingTask {

    private static final Logger log = LoggerFactory.getLogger(ReindexingTask.class);

    private int pageSize;
    private CaseRepository caseRepository;
    private TaskRepository taskRepository;
    private ElasticCaseRepository elasticCaseRepository;
    private IElasticCaseService elasticCaseService;
    private IElasticTaskService elasticTaskService;
    private IElasticCaseMappingService caseMappingService;
    private IElasticTaskMappingService taskMappingService;
    private IWorkflowService workflowService;

    private LocalDateTime lastRun;

    @Autowired
    public ReindexingTask(
            CaseRepository caseRepository,
            TaskRepository taskRepository,
            ElasticCaseRepository elasticCaseRepository,
            @Qualifier("reindexingTaskElasticCaseService")
            IElasticCaseService elasticCaseService,
            @Qualifier("reindexingTaskElasticTaskService")
            IElasticTaskService elasticTaskService,
            IElasticCaseMappingService caseMappingService,
            IElasticTaskMappingService taskMappingService,
            IWorkflowService workflowService,
            @Value("${spring.data.elasticsearch.reindexExecutor.size:20}") int pageSize,
            @Value("${spring.data.elasticsearch.reindex-from:#{null}}") Duration from) {
        this.caseRepository = caseRepository;
        this.taskRepository = taskRepository;
        this.elasticCaseRepository = elasticCaseRepository;
        this.elasticCaseService = elasticCaseService;
        this.elasticTaskService = elasticTaskService;
        this.caseMappingService = caseMappingService;
        this.taskMappingService = taskMappingService;
        this.workflowService = workflowService;
        this.pageSize = pageSize;

        lastRun = LocalDateTime.now();
        if (from != null) {
            lastRun = lastRun.minus(from);
        }
    }

    @Scheduled(cron = "#{springElasticsearchReindex}")
    public void reindex() {
        log.info("Reindexing stale cases: started reindexing after " + lastRun);

        BooleanExpression predicate = QCase.case$.lastModified.before(LocalDateTime.now()).and(QCase.case$.lastModified.after(lastRun.minusMinutes(2)));

        lastRun = LocalDateTime.now();
        long count = caseRepository.count(predicate);
        if (count > 0) {
            reindexAllPages(predicate, count);
        }

        log.info("Reindexing stale cases: end");
    }

    private void reindexAllPages(BooleanExpression predicate, long count) {
        long numOfPages = ((count / pageSize) + 1);
        log.info("Reindexing " + numOfPages + " pages");

        for (int page = 0; page < numOfPages; page++) {
            reindexPage(predicate, page, numOfPages, false);
        }
    }

    public void forceReindexPage(Predicate predicate, int page, long numOfPages) {
        reindexPage(predicate, page, numOfPages, true);
    }

    private void reindexPage(Predicate predicate, int page, long numOfPages, boolean forced) {
        log.info("Reindexing " + (page + 1) + " / " + numOfPages);
        Page<Case> cases = this.workflowService.search(predicate, PageRequest.of(page, pageSize));

        for (Case aCase : cases) {
            if (forced || elasticCaseRepository.countByStringIdAndLastModified(aCase.getStringId(), Timestamp.valueOf(aCase.getLastModified()).getTime()) == 0) {
                elasticCaseService.indexNow(this.caseMappingService.transform(aCase));
                List<Task> tasks = taskRepository.findAllByCaseId(aCase.getStringId());
                for (Task task : tasks) {
                    elasticTaskService.indexNow(this.taskMappingService.transform(task));
                }
            }
        }
    }
}
