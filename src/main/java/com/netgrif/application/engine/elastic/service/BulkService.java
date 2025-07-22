package com.netgrif.application.engine.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.netgrif.application.engine.elastic.domain.ElasticCase;
import com.netgrif.application.engine.elastic.domain.ElasticTask;
import com.netgrif.application.engine.elastic.service.interfaces.*;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service responsible for bulk indexing of {@link Case} and {@link Task} entities into Elasticsearch.
 * Uses transformation services to map domain objects to their corresponding Elastic representations.
 *
 * Indexing is performed using upsert operations.
 */
@Service
@Slf4j
public class BulkService implements IBulkService {
    @Value("${spring.data.elasticsearch.index.task}")
    private String taskIndex;

    @Value("${spring.data.elasticsearch.index.case}")
    private String caseIndex;

    @Value("${spring.data.elasticsearch.reindexExecutor.caseSize:20}")
    private int caseBatchSize;

    @Value("${spring.data.elasticsearch.reindexExecutor.taskSize:20}")
    private int taskBatchSize;

    private final ElasticsearchClient esClient;

    private final IElasticCaseMappingService elasticCaseMappingService;

    private final IElasticTaskMappingService elasticTaskMappingService;

    private final TaskRepository taskRepository;

    private final List<Case> bulkCases = new ArrayList<>();
    private final List<String> bulkCaseIds = new ArrayList<>();
    private List<Task> bulkTasks = new ArrayList<>();

    private BulkRequest.Builder builder = new BulkRequest.Builder();


    BulkService (@Qualifier("elasticsearchClient") ElasticsearchClient elasticsearchClient,
                 IElasticCaseMappingService elasticCaseMappingService,
                 IElasticTaskMappingService elasticTaskMappingService,
                 TaskRepository taskRepository) {
        this.esClient = elasticsearchClient;
        this.elasticCaseMappingService = elasticCaseMappingService;
        this.elasticTaskMappingService = elasticTaskMappingService;
        this.taskRepository = taskRepository;
    }

    /**
     * Creates elastic upsert operation for given case — if a document exists, it is updated; otherwise, it is created.
     * calls indexCases if size of case list in cache equals batch size
     *
     * @param aCase the case entities to be indexed
     */
    @Override
    public void bulkIndexCase(Case aCase) {
        if (aCase == null) return;

        bulkCases.add(aCase);
        bulkCaseIds.add(aCase.getStringId());

        try {
            if (aCase.getLastModified() == null)
                aCase.setLastModified(LocalDateTime.now());

            ElasticCase doc = elasticCaseMappingService.transform(aCase);

            builder.operations(op -> op
                    .update(u -> u
                            .index(caseIndex)
                            .id(doc.getStringId())
                            .action(a -> a
                                    .doc(doc)
                                    .docAsUpsert(true)
                            )
                    )
            );
        } catch (Exception e) {
            log.error("Failed to prepare bulk operation for case [{}]: {}", aCase.getStringId(), e.getMessage());
        }

        if (bulkCases.size() == caseBatchSize) {
            indexCases();
        }
    }

    /**
     * Calls bulkIndexTasks with empty list.
     *
     */
    @Override
    public void bulkIndexTasks() {
        bulkIndexTasks(List.of());
        bulkTasks.clear();
    }

    /**
     * Performs bulk indexing of a list of {@link Task} objects into the Elasticsearch task index.
     * Uses upsert semantics — if a document exists, it is updated; otherwise, it is created.
     *
     * @param tasks the list of task entities to be indexed
     */
    @Override
    public void bulkIndexTasks(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) return;

        tasks.addAll(0, bulkTasks);
        int totalSize = tasks.size();

        for (int i = 0; i < totalSize; i += taskBatchSize) {
            int end = Math.min(i + taskBatchSize, totalSize);
            List<Task> batch = tasks.subList(i, end);

            if (batch.size() < taskBatchSize && !tasks.isEmpty()) {
                bulkTasks = batch;
                break;
            }

            indexTaskBatch(batch);
        }
    }

    /**
     * Performs bulk indexing of a list of {@link Case} objects from cache into the Elasticsearch case index.
     * Clears cache lists and recreates {@link BulkRequest.Builder}
     */
    @Override
    public void indexCases() {
        if (bulkCases.isEmpty()) {
            return;
        }

        executeAndValidate(builder.build());
        List<Task> tasksToReindex = taskRepository.findAllByCaseIdIn(bulkCaseIds);
        bulkIndexTasks(tasksToReindex);

        bulkCases.clear();
        bulkCaseIds.clear();
        this.builder = new BulkRequest.Builder();
    }

    private void executeAndValidate(BulkRequest request) {
        try {
            BulkResponse response = esClient.bulk(request);

            checkForBulkUpdateFailure(response);
        } catch (Exception e) {
            log.error("Failed to index bulk {}", e.getMessage(), e);
        }
    }

    private void checkForBulkUpdateFailure(BulkResponse response) {
        Map<String, String> failedDocuments = new HashMap<>();


        response.items().forEach(item -> {
            if (item.error() != null) {
                failedDocuments.put(item.id(), item.error().reason());
            }
        });

        if (!failedDocuments.isEmpty()) {
            throw new ElasticsearchException("Bulk indexing has failures. Use ElasticsearchException.getFailedDocuments() for details [{}]", failedDocuments);
        }
    }

    private void indexTaskBatch(List<Task> tasks) {
        BulkRequest.Builder requestBuilder = new BulkRequest.Builder();

        for (Task task : tasks) {
            try {
                ElasticTask elasticTask = elasticTaskMappingService.transform(task);

                requestBuilder.operations(op -> op
                        .update(u -> u
                                .index(taskIndex)
                                .id(elasticTask.getStringId())
                                .action(a -> a
                                        .doc(elasticTask)
                                        .docAsUpsert(true)
                                )
                        )
                );
            } catch (Exception e) {
                log.error("Failed to create upsert request for task [{}]: {}", task.getStringId(), e.getMessage());
            }
        }

        executeAndValidate(requestBuilder.build());
    }
}
