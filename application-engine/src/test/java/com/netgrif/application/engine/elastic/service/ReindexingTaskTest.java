package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticIndexService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.objects.elastic.domain.ElasticCase;
import com.netgrif.application.engine.objects.elastic.domain.ElasticTask;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReindexingTaskTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ElasticCaseRepository elasticCaseRepository;

    @Mock
    private IElasticCaseService elasticCaseService;

    @Mock
    private IElasticTaskService elasticTaskService;

    @Mock
    private IElasticCaseMappingService caseMappingService;

    @Mock
    private IElasticTaskMappingService taskMappingService;

    @Mock
    private IWorkflowService workflowService;

    @Mock
    private IElasticIndexService elasticIndexService;

    @Test
    void rejectsInvalidPageSize() {
        DataConfigurationProperties.ElasticsearchProperties properties = properties(0, null);

        assertThrows(IllegalArgumentException.class, () -> task(properties));
    }

    @Test
    void rejectsNegativeReindexWindow() {
        DataConfigurationProperties.ElasticsearchProperties properties = properties(10, Duration.ofSeconds(-1));

        assertThrows(IllegalArgumentException.class, () -> task(properties));
    }

    @Test
    void scheduledReindexDelegatesToBulkIndexWithCalculatedLastRun() {
        ReindexingTask task = task(properties(10, Duration.ofHours(1)));

        task.reindex();

        verify(elasticIndexService).bulkIndex(eq(false), any(LocalDateTime.class), isNull(), isNull());
    }

    @Test
    void forceReindexPageIndexesCasesAndTasks() {
        ReindexingTask reindexingTask = task(properties(2, null));
        Predicate predicate = mock(Predicate.class);
        Case useCase = mock(Case.class);
        Task task = mock(Task.class);
        ElasticCase elasticCase = new ElasticCase() {
        };
        ElasticTask elasticTask = new ElasticTask() {
        };

        when(useCase.getStringId()).thenReturn("case-1");
        when(workflowService.search(predicate, PageRequest.of(1, 2))).thenReturn(new PageImpl<>(List.of(useCase)));
        when(caseMappingService.transform(useCase)).thenReturn(elasticCase);
        when(taskRepository.findAllByCaseId("case-1")).thenReturn(List.of(task));
        when(taskMappingService.transform(task)).thenReturn(elasticTask);

        reindexingTask.forceReindexPage(predicate, 1, 3);

        verify(elasticCaseService).indexNow(elasticCase);
        verify(elasticTaskService).indexNow(elasticTask);
        verify(elasticCaseRepository, never()).countByIdAndLastModified(eq("case-1"), anyLong());
    }

    private ReindexingTask task(DataConfigurationProperties.ElasticsearchProperties properties) {
        return new ReindexingTask(
                taskRepository,
                elasticCaseRepository,
                elasticCaseService,
                elasticTaskService,
                caseMappingService,
                taskMappingService,
                workflowService,
                properties,
                elasticIndexService
        );
    }

    private DataConfigurationProperties.ElasticsearchProperties properties(int pageSize, Duration reindexFrom) {
        DataConfigurationProperties.ElasticsearchProperties properties = new DataConfigurationProperties.ElasticsearchProperties();
        properties.getReindexExecutor().setSize(pageSize);
        properties.setReindexFrom(reindexFrom);
        return properties;
    }
}
