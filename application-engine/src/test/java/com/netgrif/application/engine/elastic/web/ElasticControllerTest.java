package com.netgrif.application.engine.elastic.web;

import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.elastic.service.ReindexingTask;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticIndexService;
import com.netgrif.application.engine.elastic.web.requestbodies.IndexParams;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.workflow.service.CaseSearchService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticControllerTest {

    @Mock
    private IWorkflowService workflowService;

    @Mock
    private CaseSearchService searchService;

    @Mock
    private ReindexingTask reindexingTask;

    @Mock
    private IElasticIndexService indexService;

    @Mock
    private Authentication authentication;

    @Mock
    private LoggedUser user;

    private ElasticController controller;
    private DataConfigurationProperties.ElasticsearchProperties properties;

    @BeforeEach
    void setUp() {
        properties = new DataConfigurationProperties.ElasticsearchProperties();
        properties.getReindexExecutor().setSize(2);
        controller = new ElasticController();
        controller.setWorkflowService(workflowService);
        controller.setSearchService(searchService);
        controller.setReindexingTask(reindexingTask);
        controller.setElasticsearchProperties(properties);
        controller.setIndexService(indexService);
    }

    @Test
    void reindexSchedulesEveryPageWithConfiguredPageSize() {
        when(authentication.getPrincipal()).thenReturn(user);
        Map<String, Object> body = Map.of("fullText", "invoice");
        Predicate predicate = mock(Predicate.class);
        when(workflowService.count(body, user, Locale.ENGLISH)).thenReturn(5L);
        when(searchService.buildQuery(body, user, Locale.ENGLISH)).thenReturn(predicate);

        MessageResource response = controller.reindex(body, authentication, Locale.ENGLISH);

        assertEquals("Success", response.getContent().getSuccess());
        verify(reindexingTask).forceReindexPage(predicate, 0, 3L);
        verify(reindexingTask).forceReindexPage(predicate, 1, 3L);
        verify(reindexingTask).forceReindexPage(predicate, 2, 3L);
    }

    @Test
    void reindexDoesNothingWhenNoCasesMatch() {
        when(authentication.getPrincipal()).thenReturn(user);
        Map<String, Object> body = Map.of("fullText", "missing");
        when(workflowService.count(body, user, Locale.ENGLISH)).thenReturn(0L);

        MessageResource response = controller.reindex(body, authentication, Locale.ENGLISH);

        assertEquals("Success", response.getContent().getSuccess());
        verifyNoInteractions(searchService, reindexingTask);
    }

    @Test
    void reindexReportsInvalidPageSize() {
        when(authentication.getPrincipal()).thenReturn(user);
        properties.getReindexExecutor().setSize(0);
        Map<String, Object> body = Map.of("fullText", "invoice");
        when(workflowService.count(body, user, Locale.ENGLISH)).thenReturn(1L);

        MessageResource response = controller.reindex(body, authentication, Locale.ENGLISH);

        assertEquals("Reindex executor size must be greater than 0", response.getContent().getError());
        verify(reindexingTask, never()).forceReindexPage(any(Predicate.class), anyInt(), anyLong());
    }

    @Test
    void bulkReindexDelegatesToIndexService() {
        IndexParams params = new IndexParams();
        LocalDateTime lastRun = LocalDateTime.of(2026, 1, 2, 3, 4);
        params.setIndexAll(true);
        params.setLastRun(lastRun);
        params.setCaseBatchSize(10);
        params.setTaskBatchSize(20);

        MessageResource response = controller.bulkReindex(params);

        assertEquals("Success", response.getContent().getSuccess());
        verify(indexService).bulkIndex(true, lastRun, 10, 20);
    }
}
