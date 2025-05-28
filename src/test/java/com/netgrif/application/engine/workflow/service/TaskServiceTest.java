package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.MockService;
import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.domain.params.ImportProcessParams;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.SuperCreator;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.domain.params.TaskParams;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class TaskServiceTest {

    @Autowired
    private ITaskService service;

    @Autowired
    private MockService mockService;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private PetriNetRepository petriNetRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreator superCreator;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private TaskService taskService;

    @BeforeEach
    public void setUp() throws Exception {
        testHelper.truncateDbs();

        petriNetService.importProcess(new ImportProcessParams(new FileInputStream("src/test/resources/prikladFM.xml"), VersionType.MAJOR,
                superCreator.getLoggedSuper().getActiveActorId()));
        Process net = petriNetRepository.findAll().get(0);
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .process(net)
                .title("Storage Unit")
                .authorId(mockService.mockLoggedIdentity().getActiveActorId())
                .build();
        workflowService.createCase(createCaseParams);
        TestHelper.login(superCreator.getSuperIdentity());
    }

    @Test
    public void resetArcTest() throws TransitionNotExecutableException, MissingPetriNetMetaDataException, IOException, MissingIconKeyException {
        Process net = petriNetService.importProcess(new ImportProcessParams(new FileInputStream("src/test/resources/reset_inhibitor_test.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper().getActiveActorId())).getProcess();
        LoggedIdentity mockedLoggedIdentity = mockService.mockLoggedIdentity();
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .process(net)
                .title("Reset test")
                .authorId(mockedLoggedIdentity.getActiveActorId())
                .build();
        CreateCaseEventOutcome outcome = workflowService.createCase(createCaseParams);

        Identity identity = importHelper.createIdentity(IdentityParams.with()
                .firstname(new TextField("firstname"))
                .lastname(new TextField("lastname"))
                .password(new TextField("password"))
                .username(new TextField("email@email.com"))
                .build(), new ArrayList<>());

        assert outcome.getCase().getConsumedTokens().isEmpty();
        assert outcome.getCase().getActivePlaces().size() == 1;
        assert outcome.getCase().getActivePlaces().containsValue(5);

        Task task = taskRepository.findAll().stream().filter(t -> t.getTitle().getDefaultValue().equalsIgnoreCase("reset")).findFirst().orElse(null);

        assert task != null;

        service.assignTask(new TaskParams(task.getStringId(), identity.toSession().getActiveActorId()));
        Case useCase = caseRepository.findById(outcome.getCase().getStringId()).get();

        assert useCase.getConsumedTokens().size() == 1;
        assert useCase.getConsumedTokens().containsValue(5);
        assert useCase.getActivePlaces().isEmpty();

        service.cancelTask(new TaskParams(task.getStringId(), identity.toSession().getActiveActorId()));
        useCase = caseRepository.findById(useCase.getStringId()).get();

        assert useCase.getConsumedTokens().isEmpty();
        assert useCase.getActivePlaces().size() == 1;
        assert useCase.getActivePlaces().containsValue(5);
    }

    @Test
    public void testTransactionalAssignTaskFailure() throws IOException, MissingPetriNetMetaDataException {
        String taskId = createCaseAndReturnTaskId("src/test/resources/transactional_task_event_test.xml",
                "assignTest");

        TaskParams taskParams = TaskParams.with()
                .isTransactional(true)
                .taskId(taskId)
                .assigneeId(superCreator.getLoggedSuper().getActiveActorId())
                .build();

        assertThrows(RuntimeException.class, () -> taskService.assignTask(taskParams));

        Task aTask = taskService.findOne(taskId);
        assert aTask.getAssigneeId() == null;
        assert workflowService.searchOne(QCase.case$.title.eq("CasePre")) == null;
        assert workflowService.searchOne(QCase.case$.title.eq("CasePost")) == null;
    }

    @Test
    public void testNonTransactionalAssignTaskFailure() throws IOException, MissingPetriNetMetaDataException {
        String taskId = createCaseAndReturnTaskId("src/test/resources/transactional_task_event_test.xml",
                "assignTest");

        TaskParams taskParams = TaskParams.with()
                .isTransactional(false)
                .taskId(taskId)
                .assigneeId(superCreator.getLoggedSuper().getActiveActorId())
                .build();

        assertThrows(RuntimeException.class, () -> taskService.assignTask(taskParams));

        Task aTask = taskService.findOne(taskId);
        assert aTask.getAssigneeId() != null;
        assert workflowService.searchOne(QCase.case$.title.eq("CasePre")) != null;
        assert workflowService.searchOne(QCase.case$.title.eq("CasePost")) != null;
    }

    @Test
    public void testTransactionalCancelTaskFailure() throws IOException, MissingPetriNetMetaDataException, TransitionNotExecutableException {
        String taskId = createCaseAndReturnTaskId("src/test/resources/transactional_task_event_test.xml",
                "cancelTest");

        TaskParams taskParams = TaskParams.with()
                .isTransactional(false)
                .taskId(taskId)
                .assigneeId(superCreator.getLoggedSuper().getActiveActorId())
                .build();

        Task aTask = taskService.assignTask(taskParams).getTask();
        assert Objects.equals(aTask.getAssigneeId(), superCreator.getLoggedSuper().getActiveActorId());

        taskParams.setIsTransactional(true);
        assertThrows(RuntimeException.class, () -> taskService.cancelTask(taskParams));

        aTask = taskService.findOne(taskId);
        assert Objects.equals(aTask.getAssigneeId(), superCreator.getLoggedSuper().getActiveActorId());
        assert workflowService.searchOne(QCase.case$.title.eq("CasePre")) == null;
        assert workflowService.searchOne(QCase.case$.title.eq("CasePost")) == null;
    }

    @Test
    public void testNonTransactionalCancelTaskFailure() throws IOException, MissingPetriNetMetaDataException, TransitionNotExecutableException {
        String taskId = createCaseAndReturnTaskId("src/test/resources/transactional_task_event_test.xml",
                "cancelTest");

        TaskParams taskParams = TaskParams.with()
                .isTransactional(false)
                .taskId(taskId)
                .assigneeId(superCreator.getLoggedSuper().getActiveActorId())
                .build();

        Task aTask = taskService.assignTask(taskParams).getTask();
        assert Objects.equals(aTask.getAssigneeId(), superCreator.getLoggedSuper().getActiveActorId());

        assertThrows(RuntimeException.class, () -> taskService.cancelTask(taskParams));

        aTask = taskService.findOne(taskId);
        assert aTask.getAssigneeId() == null;
        assert workflowService.searchOne(QCase.case$.title.eq("CasePre")) != null;
        assert workflowService.searchOne(QCase.case$.title.eq("CasePost")) != null;
    }

    @Test
    public void testTransactionalFinishTaskFailure() throws IOException, MissingPetriNetMetaDataException, TransitionNotExecutableException {
        String taskId = createCaseAndReturnTaskId("src/test/resources/transactional_task_event_test.xml",
                "finishTest");

        TaskParams taskParams = TaskParams.with()
                .isTransactional(false)
                .taskId(taskId)
                .assigneeId(superCreator.getLoggedSuper().getActiveActorId())
                .build();

        Task aTask = taskService.assignTask(taskParams).getTask();
        assert Objects.equals(aTask.getAssigneeId(), superCreator.getLoggedSuper().getActiveActorId());

        taskParams.setIsTransactional(true);
        assertThrows(RuntimeException.class, () -> taskService.finishTask(taskParams));

        aTask = taskService.findOne(taskId);
        assert Objects.equals(aTask.getAssigneeId(), superCreator.getLoggedSuper().getActiveActorId());
        assert aTask.getFinishedBy() == null;
        assert workflowService.searchOne(QCase.case$.title.eq("CasePre")) == null;
        assert workflowService.searchOne(QCase.case$.title.eq("CasePost")) == null;
    }

    @Test
    public void testNonTransactionalFinishTaskFailure() throws IOException, MissingPetriNetMetaDataException, TransitionNotExecutableException {
        String taskId = createCaseAndReturnTaskId("src/test/resources/transactional_task_event_test.xml",
                "finishTest");

        TaskParams taskParams = TaskParams.with()
                .isTransactional(false)
                .taskId(taskId)
                .assigneeId(superCreator.getLoggedSuper().getActiveActorId())
                .build();

        Task aTask = taskService.assignTask(taskParams).getTask();
        assert Objects.equals(aTask.getAssigneeId(), superCreator.getLoggedSuper().getActiveActorId());

        assertThrows(RuntimeException.class, () -> taskService.finishTask(taskParams));

        aTask = taskService.findOne(taskId);
        assert aTask.getAssigneeId() == null;
        assert aTask.getFinishedBy().equals(superCreator.getLoggedSuper().getActiveActorId());
        assert workflowService.searchOne(QCase.case$.title.eq("CasePre")) != null;
        assert workflowService.searchOne(QCase.case$.title.eq("CasePost")) != null;
    }

    private String createCaseAndReturnTaskId(String filePath, String transId) throws IOException, MissingPetriNetMetaDataException {
        Process process = petriNetService.importProcess(new ImportProcessParams(
                new FileInputStream(filePath), VersionType.MAJOR, superCreator.getLoggedSuper().getActiveActorId())).getProcess();

        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .process(process)
                .authorId(superCreator.getLoggedSuper().getActiveActorId())
                .build();
        Case useCase = workflowService.createCase(createCaseParams).getCase();
        return useCase.getTaskStringId(transId);
    }
}