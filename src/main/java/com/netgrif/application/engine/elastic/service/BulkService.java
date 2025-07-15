package com.netgrif.application.engine.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.netgrif.application.engine.elastic.domain.ElasticCase;
import com.netgrif.application.engine.elastic.domain.ElasticTask;
import com.netgrif.application.engine.elastic.service.interfaces.*;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final ElasticsearchClient esClient;

    private final IElasticCaseMappingService elasticCaseMappingService;

    private final IElasticTaskMappingService elasticTaskMappingService;


    BulkService (@Qualifier("elasticsearchClient") ElasticsearchClient elasticsearchClient,
                 IElasticCaseMappingService elasticCaseMappingService,
                 IElasticTaskMappingService elasticTaskMappingService) {
        this.esClient = elasticsearchClient;
        this.elasticCaseMappingService = elasticCaseMappingService;
        this.elasticTaskMappingService = elasticTaskMappingService;
    }

    /**
     * Performs bulk indexing of a list of {@link Case} objects into the Elasticsearch case index.
     * Uses upsert semantics — if a document exists, it is updated; otherwise, it is created.
     *
     * @param cases the list of case entities to be indexed
     */
    @Override
    public void bulkIndexCases(List<Case> cases) {
        BulkRequest.Builder builder = new BulkRequest.Builder();

        for (Case c : cases) {
            try {
                if (c.getLastModified() == null)
                    c.setLastModified(LocalDateTime.now());

                ElasticCase doc = elasticCaseMappingService.transform(c);

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
                log.error("Failed to prepare bulk operation for case [{}]: {}", c.getStringId(), e.getMessage());
            }
        }

        executeAndValidate(builder.build());
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

        log.info("Indexing {} tasks", tasks.size());

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

    private void executeAndValidate(BulkRequest request) {
        try {
            BulkResponse response = esClient.bulk(request);
            checkForBulkUpdateFailure(response);
        } catch (Exception e) {
            log.error("Failed to index bulk " + e.getMessage(), e);
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
            throw new ElasticsearchException(
                    "Bulk indexing has failures. Use ElasticsearchException.getFailedDocuments() for details [" +
                            failedDocuments + "]",
                    failedDocuments
            );
        }
    }
}
