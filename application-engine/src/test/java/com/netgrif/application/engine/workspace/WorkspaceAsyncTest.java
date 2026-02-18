package com.netgrif.application.engine.workspace;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.concurrent.NaeExecutors;
import com.netgrif.application.engine.concurrent.NaeThread;
import com.netgrif.application.engine.concurrent.NaeThreadPoolExecutor;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
import com.netgrif.application.engine.workflow.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.params.TaskParams;
import com.netgrif.application.engine.workflow.service.TaskService;
import com.netgrif.application.engine.workflow.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class WorkspaceAsyncTest {

    private static final String activeWorkspaceId = "default";

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private SuperCreatorRunner superCreatorRunner;

    @BeforeEach
    protected void beforeEach() {
        testHelper.truncateDbs();
        loggedCustomUser();
    }

    private void loggedCustomUser() {
        LoggedUser loggedUser = new LoggedUserImpl();
        loggedUser.setUsername("username1");
        loggedUser.setActiveWorkspaceId(activeWorkspaceId);
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(loggedUser, "password", null));

    }

    @Test
    public void testThreadContextPropagationFailureWithExecutor() throws ExecutionException, InterruptedException, TimeoutException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);

        CompletableFuture<Authentication> futureResp = CompletableFuture.supplyAsync(() -> SecurityContextHolder.getContext().getAuthentication(), executorService);
        assertNull(futureResp.get(1000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testThreadContextPropagationFailureWithThread() throws InterruptedException {

        AtomicReference<Authentication> authFromContext = new AtomicReference<>(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(authFromContext.get());

        Thread t = new Thread(() -> authFromContext.set(SecurityContextHolder.getContext().getAuthentication()));
        t.start();
        t.join(1000);
        assertNull(authFromContext.get());
    }

    @Test
    public void testThreadContextPropagationSuccessWithExecutor() throws ExecutionException, InterruptedException, TimeoutException {
        ExecutorService executorService = NaeExecutors.newSingleThreadExecutor();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(activeWorkspaceId, ((LoggedUser) auth.getPrincipal()).getActiveWorkspaceId());

        CompletableFuture<Authentication> futureResp = CompletableFuture.supplyAsync(() -> SecurityContextHolder.getContext().getAuthentication(), executorService);
        Authentication authFromFuture = futureResp.get(1000, TimeUnit.MILLISECONDS);
        assertNotNull(authFromFuture);
        assertEquals(activeWorkspaceId, ((LoggedUser) authFromFuture.getPrincipal()).getActiveWorkspaceId());
    }

    @Test
    public void testThreadContextPropagationSuccessWithThreadPool() throws ExecutionException, InterruptedException, TimeoutException {
        ExecutorService executorService = new NaeThreadPoolExecutor(1, 2, 1000,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(activeWorkspaceId, ((LoggedUser) auth.getPrincipal()).getActiveWorkspaceId());

        CompletableFuture<Authentication> complFutureResp = CompletableFuture.supplyAsync(() -> SecurityContextHolder.getContext().getAuthentication(), executorService);
        Authentication authFromFuture = complFutureResp.get(1000, TimeUnit.MILLISECONDS);
        assertNotNull(authFromFuture);
        assertEquals(activeWorkspaceId, ((LoggedUser) authFromFuture.getPrincipal()).getActiveWorkspaceId());

        Future<Authentication> futureResp = executorService.submit(() -> SecurityContextHolder.getContext().getAuthentication());
        authFromFuture = futureResp.get(1000, TimeUnit.MILLISECONDS);
        assertNotNull(authFromFuture);
        assertEquals(activeWorkspaceId, ((LoggedUser) authFromFuture.getPrincipal()).getActiveWorkspaceId());
    }

    @Test
    public void testThreadContextPropagationSuccessWithThread() throws InterruptedException {
        AtomicReference<Authentication> authFromContext = new AtomicReference<>(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(authFromContext.get());
        assertEquals(activeWorkspaceId, ((LoggedUser) authFromContext.get().getPrincipal()).getActiveWorkspaceId());

        Thread t = new NaeThread(() -> authFromContext.set(SecurityContextHolder.getContext().getAuthentication()));
        t.start();
        t.join(1000);
        assertNotNull(authFromContext.get());
        assertEquals(activeWorkspaceId, ((LoggedUser) authFromContext.get().getPrincipal()).getActiveWorkspaceId());
    }

    @Test
    public void testThreadContextPropagationWithAction() throws TransitionNotExecutableException, InterruptedException {
        importHelper.createNet("workspace_async_test.xml");
        Case testCase = workflowService.createCase(CreateCaseParams.with()
                .processIdentifier("workspace_async_test")
                .author(superCreatorRunner.getSuperUser())
                .build()).getCase();

        String taskId = testCase.findTaskId("t1").get();
        taskService.assignTask(new TaskParams(taskId));
        taskService.finishTask(new TaskParams(taskId));

        NaeThread.sleep(2000);
        testCase = workflowService.findOne(testCase.getStringId());
        assertEquals("username1", testCase.getFieldValue("username"));
        assertEquals(activeWorkspaceId, testCase.getFieldValue("activeWorkspaceId"));
    }

}
