package com.netgrif.application.engine.workflow;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.VersionType;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
import com.netgrif.application.engine.startup.runner.SystemUserRunner;
import com.netgrif.application.engine.workflow.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.params.DelegateTaskParams;
import com.netgrif.application.engine.workflow.params.TaskParams;
import com.netgrif.application.engine.workflow.service.TaskService;
import com.netgrif.application.engine.workflow.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

@Slf4j
@Disabled
@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class WorkflowPerformanceTest {

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreatorRunner superCreatorRunner;

    @Autowired
    private SystemUserRunner systemUserRunner;

    @Autowired
    private TestHelper testHelper;

    @BeforeEach
    public void beforeEach() {
        testHelper.truncateDbs();
    }

    @Test
    public void testCreatePerformance() throws IOException, MissingPetriNetMetaDataException {
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/test_create_case_performance.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();
        iterateAndShowAvgTime("createCase", () -> workflowService.createCase(CreateCaseParams.with()
                .process(net)
                .author(superCreatorRunner.getLoggedSuper())
                .locale(Locale.getDefault())
                .build()), 1000);
    }

    @Test
    public void testCreateWithActionPerformance() throws IOException, MissingPetriNetMetaDataException {
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/test_create_case_performance_with_action.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();
        iterateAndShowAvgTime("createCaseWithAction", () -> workflowService.createCase(CreateCaseParams.with()
                .process(net)
                .author(superCreatorRunner.getLoggedSuper())
                .locale(Locale.getDefault())
                .build()), 5000);
    }

    @Test
    public void testAssignPerformance() throws IOException, MissingPetriNetMetaDataException, TransitionNotExecutableException {
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/test_task_event.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();
        long totalElapsedTime = 0;
        int iterations = 5000;
        LoggedUser loggedUser = superCreatorRunner.getLoggedSuper();

        for (int i = 0; i < iterations; i++) {
            Case useCase = workflowService.createCase(CreateCaseParams.with()
                    .process(net)
                    .author(loggedUser)
                    .locale(Locale.getDefault())
                    .build()).getCase();
            String taskId = useCase.getTasks().stream().findFirst().get().getTask();
            long start = System.currentTimeMillis();
            taskService.assignTask(TaskParams.with()
                    .taskId(taskId)
                    .useCase(useCase)
                    .build());
            long finish = System.currentTimeMillis();
            totalElapsedTime += finish - start;
        }
        log.info("AVG time for event [assignTask] is [{} ms] for [{}] iterations", totalElapsedTime / iterations, iterations);
    }

    @Test
    public void testAssignWithActionPerformance() throws IOException, MissingPetriNetMetaDataException, TransitionNotExecutableException {
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/test_task_event_with_action.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();
        long totalElapsedTime = 0;
        int iterations = 5000;
        LoggedUser loggedUser = superCreatorRunner.getLoggedSuper();

        for (int i = 0; i < iterations; i++) {
            Case useCase = workflowService.createCase(CreateCaseParams.with()
                    .process(net)
                    .author(loggedUser)
                    .locale(Locale.getDefault())
                    .build()).getCase();
            String taskId = useCase.getTasks().stream().findFirst().get().getTask();
            long start = System.currentTimeMillis();
            taskService.assignTask(TaskParams.with()
                    .taskId(taskId)
                    .useCase(useCase)
                    .build());
            long finish = System.currentTimeMillis();
            totalElapsedTime += finish - start;
        }
        log.info("AVG time for event [assignTaskWithAction] is [{} ms] for [{}] iterations", totalElapsedTime / iterations, iterations);
    }

    @Test
    public void testCancelPerformance() throws IOException, MissingPetriNetMetaDataException, TransitionNotExecutableException {
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/test_task_event.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();
        long totalElapsedTime = 0;
        int iterations = 5000;
        LoggedUser loggedUser = superCreatorRunner.getLoggedSuper();

        for (int i = 0; i < iterations; i++) {
            Case useCase = workflowService.createCase(CreateCaseParams.with()
                    .process(net)
                    .author(loggedUser)
                    .locale(Locale.getDefault())
                    .build()).getCase();
            String taskId = useCase.getTasks().stream().findFirst().get().getTask();
            AssignTaskEventOutcome assignOutcome = taskService.assignTask(TaskParams.with()
                    .taskId(taskId)
                    .build());
            long start = System.currentTimeMillis();
            taskService.cancelTask(TaskParams.with()
                    .task(assignOutcome.getTask())
                    .useCase(assignOutcome.getCase())
                    .user(loggedUser)
                    .build());
            long finish = System.currentTimeMillis();
            totalElapsedTime += finish - start;
        }
        log.info("AVG time for event [cancelTask] is [{} ms] for [{}] iterations", totalElapsedTime / iterations, iterations);
    }

    @Test
    public void testCancelWithActionPerformance() throws IOException, MissingPetriNetMetaDataException, TransitionNotExecutableException {
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/test_task_event_with_action.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();
        long totalElapsedTime = 0;
        int iterations = 5000;
        LoggedUser loggedUser = superCreatorRunner.getLoggedSuper();

        for (int i = 0; i < iterations; i++) {
            Case useCase = workflowService.createCase(CreateCaseParams.with()
                    .process(net)
                    .author(loggedUser)
                    .locale(Locale.getDefault())
                    .build()).getCase();
            String taskId = useCase.getTasks().stream().findFirst().get().getTask();
            AssignTaskEventOutcome assignOutcome = taskService.assignTask(TaskParams.with()
                    .taskId(taskId)
                    .build());
            long start = System.currentTimeMillis();
            taskService.cancelTask(TaskParams.with()
                    .task(assignOutcome.getTask())
                    .useCase(assignOutcome.getCase())
                    .user(loggedUser)
                    .build());
            long finish = System.currentTimeMillis();
            totalElapsedTime += finish - start;
        }
        log.info("AVG time for event [cancelTaskWithAction] is [{} ms] for [{}] iterations", totalElapsedTime / iterations, iterations);
    }

    @Test
    public void testFinishPerformance() throws IOException, MissingPetriNetMetaDataException, TransitionNotExecutableException {
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/test_task_event.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();
        long totalElapsedTime = 0;
        int iterations = 5000;
        LoggedUser loggedUser = superCreatorRunner.getLoggedSuper();

        for (int i = 0; i < iterations; i++) {
            Case useCase = workflowService.createCase(CreateCaseParams.with()
                    .process(net)
                    .author(loggedUser)
                    .locale(Locale.getDefault())
                    .build()).getCase();
            String taskId = useCase.getTasks().stream().findFirst().get().getTask();
            AssignTaskEventOutcome assignOutcome = taskService.assignTask(TaskParams.with()
                    .taskId(taskId)
                    .user(loggedUser)
                    .build());
            long start = System.currentTimeMillis();
            taskService.finishTask(TaskParams.with()
                    .task(assignOutcome.getTask())
                    .useCase(assignOutcome.getCase())
                    .user(loggedUser)
                    .build());
            long finish = System.currentTimeMillis();
            totalElapsedTime += finish - start;
        }
        log.info("AVG time for event [finishTask] is [{} ms] for [{}] iterations", totalElapsedTime / iterations, iterations);
    }

    @Test
    public void testFinishWithActionPerformance() throws IOException, MissingPetriNetMetaDataException, TransitionNotExecutableException {
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/test_task_event_with_action.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();
        long totalElapsedTime = 0;
        int iterations = 5000;
        LoggedUser loggedUser = superCreatorRunner.getLoggedSuper();

        for (int i = 0; i < iterations; i++) {
            Case useCase = workflowService.createCase(CreateCaseParams.with()
                    .process(net)
                    .author(loggedUser)
                    .locale(Locale.getDefault())
                    .build()).getCase();
            String taskId = useCase.getTasks().stream().findFirst().get().getTask();
            AssignTaskEventOutcome assignOutcome = taskService.assignTask(TaskParams.with()
                    .taskId(taskId)
                    .user(loggedUser)
                    .build());
            long start = System.currentTimeMillis();
            taskService.finishTask(TaskParams.with()
                    .task(assignOutcome.getTask())
                    .useCase(assignOutcome.getCase())
                    .user(loggedUser)
                    .build());
            long finish = System.currentTimeMillis();
            totalElapsedTime += finish - start;
        }
        log.info("AVG time for event [finishTaskWithAction] is [{} ms] for [{}] iterations", totalElapsedTime / iterations, iterations);
    }

    @Test
    public void testDelegatePerformance() throws IOException, MissingPetriNetMetaDataException, TransitionNotExecutableException {
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/test_task_event.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();
        long totalElapsedTime = 0;
        int iterations = 5000;
        LoggedUser loggedUser = superCreatorRunner.getLoggedSuper();

        for (int i = 0; i < iterations; i++) {
            Case useCase = workflowService.createCase(CreateCaseParams.with()
                    .process(net)
                    .author(loggedUser)
                    .locale(Locale.getDefault())
                    .build()).getCase();
            String taskId = useCase.getTasks().stream().findFirst().get().getTask();
            AssignTaskEventOutcome assignOutcome = taskService.assignTask(TaskParams.with()
                    .taskId(taskId)
                    .user(loggedUser)
                    .build());
            long start = System.currentTimeMillis();
            taskService.delegateTask(DelegateTaskParams.with()
                    .task(assignOutcome.getTask())
                    .useCase(assignOutcome.getCase())
                    .newAssignee(systemUserRunner.getLoggedSystem())
                    .delegator(loggedUser)
                    .build());
            long finish = System.currentTimeMillis();
            totalElapsedTime += finish - start;
        }
        log.info("AVG time for event [delegateTask] is [{} ms] for [{}] iterations", totalElapsedTime / iterations, iterations);
    }

    @Test
    public void testDelegateWithActionPerformance() throws IOException, MissingPetriNetMetaDataException, TransitionNotExecutableException {
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/test_task_event_with_action.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();
        long totalElapsedTime = 0;
        int iterations = 5000;
        LoggedUser loggedUser = superCreatorRunner.getLoggedSuper();

        for (int i = 0; i < iterations; i++) {
            Case useCase = workflowService.createCase(CreateCaseParams.with()
                    .process(net)
                    .author(loggedUser)
                    .locale(Locale.getDefault())
                    .build()).getCase();
            String taskId = useCase.getTasks().stream().findFirst().get().getTask();
            AssignTaskEventOutcome assignOutcome = taskService.assignTask(TaskParams.with()
                    .taskId(taskId)
                    .user(loggedUser)
                    .build());
            long start = System.currentTimeMillis();
            taskService.delegateTask(DelegateTaskParams.with()
                    .task(assignOutcome.getTask())
                    .useCase(assignOutcome.getCase())
                    .newAssignee(systemUserRunner.getLoggedSystem())
                    .delegator(loggedUser)
                    .build());
            long finish = System.currentTimeMillis();
            totalElapsedTime += finish - start;
        }
        log.info("AVG time for event [delegateTaskWithAction] is [{} ms] for [{}] iterations", totalElapsedTime / iterations, iterations);
    }

    private void iterateAndShowAvgTime(String event, Runnable callback, int iterations) {
        long totalElapsedTime = 0;
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            callback.run();
            long finish = System.currentTimeMillis();
            totalElapsedTime += finish - start;
        }
        log.info("AVG time for event [{}] is [{} ms] for [{}] iterations", event, totalElapsedTime / iterations, iterations);
    }
}
