package com.netgrif.application.engine.actions;

import com.netgrif.application.engine.adapter.spring.actions.ActionFileHolder;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.dto.AuthPrincipalDto;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.CancelTaskEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.FinishTaskEventOutcome;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.workflow.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.params.DeleteCaseParams;
import com.netgrif.application.engine.workflow.params.TaskParams;
import com.netgrif.application.engine.workflow.service.FileFieldInputStream;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.node.ObjectNode;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActionApiImplUnitTest {

    @Mock
    private IDataService dataService;

    @Mock
    private ITaskService taskService;

    @Mock
    private IWorkflowService workflowService;

    @Mock
    private IElasticCaseService elasticCaseService;

    @Mock
    private IElasticTaskService elasticTaskService;

    @Mock
    private IPetriNetService petriNetService;

    @Mock
    private UserService userService;

    @Mock
    private Predicate predicate;

    private ActionApiImpl actionApi;
    private AbstractUser user;
    private AuthPrincipalDto principal;

    @BeforeEach
    void setUp() {
        ActorTransformer.setLoggedUserFactory(com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl::new);

        actionApi = new ActionApiImpl();
        actionApi.setDataService(dataService);
        actionApi.setTaskService(taskService);
        actionApi.setWorkflowService(workflowService);
        actionApi.setElasticCaseService(elasticCaseService);
        actionApi.setElasticTaskService(elasticTaskService);
        actionApi.setPetriNetService(petriNetService);
        actionApi.setUserService(userService);

        user = new com.netgrif.application.engine.adapter.spring.auth.domain.User();
        user.setUsername("john");
        user.setRealmId("realm");
        user.setFirstName("John");
        user.setLastName("Worker");
        user.setEmail("john@example.com");
        principal = new AuthPrincipalDto("john", "realm", null);
    }

    @AfterEach
    void tearDown() {
        ActorTransformer.setLoggedUserFactory(() -> {
            throw new IllegalStateException("No LoggedUserFactory configured");
        });
    }

    @Test
    void delegatesDataAndFileOperations() throws Exception {
        Map<String, String> params = Map.of("source", "unit");
        GetDataEventOutcome getDataOutcome = org.mockito.Mockito.mock(GetDataEventOutcome.class);
        SetDataEventOutcome setDataOutcome = org.mockito.Mockito.mock(SetDataEventOutcome.class);
        when(dataService.getData("task-1", params)).thenReturn(getDataOutcome);
        when(dataService.setData(eq("task-1"), any(ObjectNode.class), eq(params))).thenReturn(setDataOutcome);

        assertSame(getDataOutcome, actionApi.getData("task-1", params));
        assertSame(setDataOutcome, actionApi.setData(
                "task-1",
                Map.of("text", Map.of("type", "text", "value", "hello")),
                params
        ));

        ArgumentCaptor<ObjectNode> valuesCaptor = ArgumentCaptor.forClass(ObjectNode.class);
        verify(dataService).setData(eq("task-1"), valuesCaptor.capture(), eq(params));
        assertEquals("hello", valuesCaptor.getValue().get("text").get("value").asString());

        ActionFileHolder holder = ActionFileHolder.builder()
                .fileName("note.txt")
                .fileContent("content".getBytes(StandardCharsets.UTF_8))
                .build();
        when(dataService.saveFile(eq("task-1"), eq("file"), any(MultipartFile.class), eq(params))).thenReturn(setDataOutcome);
        assertSame(setDataOutcome, actionApi.saveFile("task-1", "file", holder, params));

        ArgumentCaptor<MultipartFile> fileCaptor = ArgumentCaptor.forClass(MultipartFile.class);
        verify(dataService).saveFile(eq("task-1"), eq("file"), fileCaptor.capture(), eq(params));
        assertEquals("note.txt", fileCaptor.getValue().getOriginalFilename());
        assertArrayEquals(holder.getFileContent(), fileCaptor.getValue().getBytes());

        when(dataService.saveFiles(eq("task-1"), eq("file"), any(MultipartFile[].class), eq(params))).thenReturn(setDataOutcome);
        assertSame(setDataOutcome, actionApi.saveFiles("task-1", "file", new ActionFileHolder[]{holder}, params));

        ArgumentCaptor<MultipartFile[]> filesCaptor = ArgumentCaptor.forClass(MultipartFile[].class);
        verify(dataService).saveFiles(eq("task-1"), eq("file"), filesCaptor.capture(), eq(params));
        assertEquals(1, filesCaptor.getValue().length);
        assertEquals("note.txt", filesCaptor.getValue()[0].getOriginalFilename());

        when(dataService.deleteFile("task-1", "file", params)).thenReturn(setDataOutcome);
        when(dataService.deleteFileByName("task-1", "file", "note.txt", params)).thenReturn(setDataOutcome);
        assertSame(setDataOutcome, actionApi.deleteFile("task-1", "file", params));
        assertSame(setDataOutcome, actionApi.deleteFileByName("task-1", "file", "note.txt", params));

        when(dataService.getFile("case-1", "file", true, params))
                .thenReturn(new FileFieldInputStream(new ByteArrayInputStream("preview".getBytes(StandardCharsets.UTF_8)), "preview.txt"));
        when(dataService.getFileByCaseAndName("case-1", "file", "note.txt", params))
                .thenReturn(new FileFieldInputStream(new ByteArrayInputStream("download".getBytes(StandardCharsets.UTF_8)), "note.txt"));

        ActionFileHolder preview = actionApi.getFile("case-1", "file", true, params);
        ActionFileHolder download = actionApi.getFileByCaseAndName("case-1", "file", "note.txt", params);

        assertEquals("preview.txt", preview.getFileName());
        assertArrayEquals("preview".getBytes(StandardCharsets.UTF_8), preview.getFileContent());
        assertEquals("note.txt", download.getFileName());
        assertArrayEquals("download".getBytes(StandardCharsets.UTF_8), download.getFileContent());
    }

    @Test
    void delegatesSearchAndCountOperations() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Case> cases = Page.empty(pageable);
        Page<Task> tasks = Page.empty(pageable);
        when(workflowService.search(predicate, pageable)).thenReturn(cases);
        when(taskService.search(predicate, pageable)).thenReturn(tasks);

        assertSame(cases, actionApi.searchCases("process", predicate, pageable));
        assertSame(tasks, actionApi.searchTasks("process", predicate, pageable));

        when(userService.findUserByUsername("john", "realm")).thenReturn(Optional.of(user));
        when(elasticCaseService.search(anyList(), any(), eq(pageable), any(Locale.class), eq(true))).thenReturn(cases);
        when(elasticTaskService.search(anyList(), any(), eq(pageable), any(Locale.class), eq(false))).thenReturn(tasks);
        when(elasticCaseService.count(anyList(), any(), any(Locale.class), eq(true))).thenReturn(7L);
        when(elasticTaskService.count(anyList(), any(), any(Locale.class), eq(false))).thenReturn(3L);

        assertSame(cases, actionApi.searchCases(List.of("case query"), principal, pageable, true));
        assertSame(tasks, actionApi.searchTasks(List.of("task query"), principal, pageable, false));
        assertEquals(7L, actionApi.countCases(List.of("case query"), principal, true));
        assertEquals(3L, actionApi.countTasks(List.of("task query"), principal, false, Map.of()));

        ArgumentCaptor<List<CaseSearchRequest>> caseRequests = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<ElasticTaskSearchRequest>> taskRequests = ArgumentCaptor.forClass(List.class);
        verify(elasticCaseService).search(caseRequests.capture(), any(), eq(pageable), any(Locale.class), eq(true));
        verify(elasticTaskService).search(taskRequests.capture(), any(), eq(pageable), any(Locale.class), eq(false));
        assertEquals("case query", caseRequests.getValue().getFirst().query);
        assertEquals("task query", taskRequests.getValue().getFirst().query);
    }

    @Test
    void delegatesCaseTaskAndUserOperations() throws Exception {
        Map<String, String> params = Map.of("source", "unit");
        Task task = new com.netgrif.application.engine.adapter.spring.workflow.domain.Task();
        Case useCase = new com.netgrif.application.engine.adapter.spring.workflow.domain.Case();
        Page<com.netgrif.application.engine.objects.auth.domain.User> users = new PageImpl<>(List.of());
        CreateCaseEventOutcome createOutcome = org.mockito.Mockito.mock(CreateCaseEventOutcome.class);
        DeleteCaseEventOutcome deleteOutcome = org.mockito.Mockito.mock(DeleteCaseEventOutcome.class);
        AssignTaskEventOutcome assignOutcome = org.mockito.Mockito.mock(AssignTaskEventOutcome.class);
        CancelTaskEventOutcome cancelOutcome = org.mockito.Mockito.mock(CancelTaskEventOutcome.class);
        FinishTaskEventOutcome finishOutcome = org.mockito.Mockito.mock(FinishTaskEventOutcome.class);

        when(userService.findUserByUsername("john", "realm")).thenReturn(Optional.of(user));
        when(taskService.findOne("task-1")).thenReturn(task);
        when(workflowService.findOne("case-1")).thenReturn(useCase);
        when(userService.search(predicate, PageRequest.of(1, 10), "realm")).thenReturn(users);
        when(workflowService.createCase(any(CreateCaseParams.class))).thenReturn(createOutcome);
        when(workflowService.deleteCase(any(DeleteCaseParams.class))).thenReturn(deleteOutcome);
        when(taskService.assignTask(any(TaskParams.class))).thenReturn(assignOutcome);
        when(taskService.cancelTask(any(TaskParams.class))).thenReturn(cancelOutcome);
        when(taskService.finishTask(any(TaskParams.class))).thenReturn(finishOutcome);

        assertSame(useCase, actionApi.findCase("case-1"));
        assertSame(task, actionApi.findTask("task-1"));
        assertSame(users, actionApi.searchUsers(predicate, PageRequest.of(1, 10), "realm"));
        assertSame(createOutcome, actionApi.createCaseByIdentifier("process", "title", "red", principal, params));
        assertSame(deleteOutcome, actionApi.deleteCase("case-1", params));
        assertSame(assignOutcome, actionApi.assignTask("task-1", principal, params));
        assertSame(cancelOutcome, actionApi.cancelTask("task-1", principal, params));
        assertSame(finishOutcome, actionApi.finishTask("task-1", principal, params));

        verify(workflowService).createCase(org.mockito.ArgumentMatchers.argThat(createCaseParams ->
                "process".equals(createCaseParams.getProcessIdentifier())
                        && "title".equals(createCaseParams.getTitle())
                        && "red".equals(createCaseParams.getColor())
                        && user.equals(createCaseParams.getAuthor())
                        && params.equals(createCaseParams.getParams())
        ));
        verify(workflowService).deleteCase(org.mockito.ArgumentMatchers.argThat(deleteCaseParams ->
                "case-1".equals(deleteCaseParams.getUseCaseId()) && params.equals(deleteCaseParams.getParams())
        ));
        verify(taskService).assignTask(org.mockito.ArgumentMatchers.argThat(taskParams ->
                task.equals(taskParams.getTask()) && user.equals(taskParams.getUser()) && params.equals(taskParams.getParams())
        ));
    }

    @Test
    void resolvesProcessAvailabilityAndInvalidPrincipals() {
        PetriNet petriNet = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet();
        when(petriNetService.getDefaultVersionByIdentifier("known")).thenReturn(petriNet);

        assertTrue(actionApi.getProcessAvailability("known").isUp());
        assertFalse(actionApi.getProcessAvailability("missing").isUp());
        assertTrue(actionApi.getProcessAvailability("missing").isNotFound());
        assertTrue(actionApi.getProcessAvailability("known", "missing").isAnyUp());
        Map<String, String> emptyParams = Map.of();
        List<String> queries = List.of("query");
        assertThrows(NullPointerException.class, () -> actionApi.getProcessAvailability((List<String>) null));
        assertThrows(IllegalArgumentException.class, () -> actionApi.createCaseByIdentifier("process", "title", "red", null, emptyParams));

        when(userService.findUserByUsername("john", "realm")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> actionApi.countCases(queries, principal, true));
    }
}
