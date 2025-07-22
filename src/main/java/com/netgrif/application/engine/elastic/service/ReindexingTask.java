package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository;
import com.netgrif.application.engine.elastic.service.interfaces.*;
import com.netgrif.application.engine.petrinet.service.PetriNetService;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.CloseableIterator;
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

    private final int pageSize;
    private final CaseRepository caseRepository;
    private final TaskRepository taskRepository;
    private final ElasticCaseRepository elasticCaseRepository;
    private final IElasticCaseService elasticCaseService;
    private final IElasticTaskService elasticTaskService;
    private final IElasticCaseMappingService caseMappingService;
    private final IElasticTaskMappingService taskMappingService;
    private final IWorkflowService workflowService;
    private final MongoTemplate mongoTemplate;
    private final PetriNetService petriNetService;
    private LocalDateTime lastRun;
    private final IBulkService bulkService;

    private  final IElasticCaseMappingService elasticCaseMappingService;

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
            IBulkService bulkService,
            MongoTemplate mongoTemplate,
            PetriNetService petriNetService,
            @Value("${spring.data.elasticsearch.reindexExecutor.caseSize:20}") int pageSize,
            @Value("${spring.data.elasticsearch.reindex-from:#{null}}") Duration from,
            IElasticCaseMappingService elasticCaseMappingService) {
        this.caseRepository = caseRepository;
        this.taskRepository = taskRepository;
        this.elasticCaseRepository = elasticCaseRepository;
        this.elasticCaseService = elasticCaseService;
        this.elasticTaskService = elasticTaskService;
        this.caseMappingService = caseMappingService;
        this.taskMappingService = taskMappingService;
        this.workflowService = workflowService;
        this.mongoTemplate = mongoTemplate;
        this.petriNetService = petriNetService;
        this.pageSize = pageSize;
        this.bulkService = bulkService;
        this.elasticCaseMappingService = elasticCaseMappingService;

        lastRun = LocalDateTime.now();
        if (from != null) {
            lastRun = lastRun.minus(from);
        }
    }

    @Scheduled(cron = "#{springElasticsearchReindex}")
    public void reindex() {
        log.info("Reindexing stale cases: started reindexing after {}", lastRun);

        LocalDateTime now = LocalDateTime.now();
        //BooleanExpression predicate = QCase.case$.lastModified.before(now).and(QCase.case$.lastModified.after(lastRun.minusMinutes(2)));
        BooleanExpression predicate = QCase.case$.lastModified.isNotNull();
        LocalDateTime lastRunOld = lastRun;
        lastRun = LocalDateTime.now();

        long count = caseRepository.count(predicate);
        if (count > 0) {
            reindexAllPages(count, now, lastRunOld);
        }

        log.info("Reindexing stale cases: end");
    }

    private void reindexAllPages(long count, LocalDateTime now, LocalDateTime lastRunOld) {
        long numOfPages = ((count / pageSize) + 1);
        log.info("Reindexing {} pages", numOfPages);
        Query query = new Query();

        query.cursorBatchSize(pageSize);

        //query.addCriteria(Criteria.where("lastModified").lt(now).gt(lastRunOld.minusMinutes(2)));

        try (CloseableIterator<Case> cursor = mongoTemplate.stream(query, Case.class)) {
            cursor.stream().forEach(aCase -> {
                /*if (elasticCaseRepository.countByStringIdAndLastModified(aCase.getStringId(), Timestamp.valueOf(aCase.getLastModified()).getTime()) == 0) {
                    return;
                }*/

                if (aCase.getPetriNet() == null) {
                    aCase.setPetriNet(petriNetService.get(aCase.getPetriNetObjectId()));
                }

                bulkService.bulkIndexCase(aCase);
            });
        }

        bulkService.indexCases();
        bulkService.bulkIndexTasks();
    }

    public void forceReindexPage(Predicate predicate, int page, long numOfPages) {
        reindexPage(predicate, page, numOfPages, true);
    }

    private void reindexPage(Predicate predicate, int page, long numOfPages, boolean forced) {
        log.info("Reindexing {} / {}", (page + 1), numOfPages);
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
