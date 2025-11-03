package com.netgrif.application.engine.workflow;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.objects.auth.domain.*;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.VersionType;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
import com.netgrif.application.engine.workflow.params.CreateCaseParams;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Slf4j
@Disabled
@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class WorkflowMvcPerformanceTest {
    private static final String ASSIGN_TASK_URL = "/api/task/assign/";
    private static final String DELEGATE_TASK_URL = "/api/task/delegate/";
    private static final String FINISH_TASK_URL = "/api/task/finish/";
    private static final String CANCEL_TASK_URL = "/api/task/cancel/";

    private MockMvc mvc;
    private PetriNet net;
    private AbstractUser user1;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreatorRunner superCreatorRunner;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @BeforeEach
    public void beforeEach() throws IOException, MissingPetriNetMetaDataException {
        testHelper.truncateDbs();
        this.net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/test_task_event.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();
        this.mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build();

        User user = new User();
        user.setFirstName("Firstname");
        user.setLastName("Lastname");
        user.setUsername("username1");
        user.setEmail("user1@netgrif.com");
        PasswordCredential passwordCredential = new PasswordCredential("password", 0, true);
        user.setCredential("password", passwordCredential);
        user.setState(UserState.ACTIVE);
        this.user1 = userService.createUser(user, null);
    }

    @Test
    void testAssignAdminPerformance() throws Exception {
        long totalElapsedTime = 0;
        int iterations = 1000;
        LoggedUser loggedUser = superCreatorRunner.getLoggedSuper();

        for (int i = 0; i < iterations; i++) {
            Case useCase = workflowService.createCase(CreateCaseParams.with()
                    .process(net)
                    .author(loggedUser)
                    .locale(Locale.getDefault())
                    .build()).getCase();
            String taskId = useCase.getTasks().stream().findFirst().get().getTask();
            long start = System.currentTimeMillis();
            mvc.perform(get(ASSIGN_TASK_URL + taskId)
                    .with(httpBasic("super@netgrif.com", "password")));
            long finish = System.currentTimeMillis();
            totalElapsedTime += finish - start;
        }
        log.info("AVG time for event [assignTask by admin] is [{} ms] for [{}] iterations", totalElapsedTime / iterations, iterations);
    }

    @Test
    void testAssignPerformance() throws Exception {
        long totalElapsedTime = 0;
        int iterations = 1000;
        LoggedUser loggedUser = superCreatorRunner.getLoggedSuper();

        for (int i = 0; i < iterations; i++) {
            Case useCase = workflowService.createCase(CreateCaseParams.with()
                    .process(net)
                    .author(loggedUser)
                    .locale(Locale.getDefault())
                    .build()).getCase();
            String taskId = useCase.getTasks().stream().findFirst().get().getTask();
            long start = System.currentTimeMillis();
            mvc.perform(get(ASSIGN_TASK_URL + taskId)
                    .with(httpBasic("user1@netgrif.com", "password")));
            long finish = System.currentTimeMillis();
            totalElapsedTime += finish - start;
        }
        log.info("AVG time for event [assignTask] is [{} ms] for [{}] iterations", totalElapsedTime / iterations, iterations);
    }

    @Test
    public void testCancelAdminPerformance() throws Exception {
        long totalElapsedTime = 0;
        int iterations = 1000;
        LoggedUser loggedUser = superCreatorRunner.getLoggedSuper();

        for (int i = 0; i < iterations; i++) {
            Case useCase = workflowService.createCase(CreateCaseParams.with()
                    .process(net)
                    .author(loggedUser)
                    .locale(Locale.getDefault())
                    .build()).getCase();
            String taskId = useCase.getTasks().stream().findFirst().get().getTask();
            taskService.assignTask(TaskParams.with()
                    .taskId(taskId)
                    .user(loggedUser)
                    .build());
            long start = System.currentTimeMillis();
            mvc.perform(get(CANCEL_TASK_URL + taskId)
                    .with(httpBasic("super@netgrif.com", "password")));
            long finish = System.currentTimeMillis();
            totalElapsedTime += finish - start;
        }
        log.info("AVG time for event [cancelTask by admin] is [{} ms] for [{}] iterations", totalElapsedTime / iterations, iterations);
    }

    @Test
    public void testCancelPerformance() throws Exception {
        long totalElapsedTime = 0;
        int iterations = 1000;
        LoggedUser loggedUser = superCreatorRunner.getLoggedSuper();

        for (int i = 0; i < iterations; i++) {
            Case useCase = workflowService.createCase(CreateCaseParams.with()
                    .process(net)
                    .author(loggedUser)
                    .locale(Locale.getDefault())
                    .build()).getCase();
            String taskId = useCase.getTasks().stream().findFirst().get().getTask();
            taskService.assignTask(TaskParams.with()
                    .taskId(taskId)
                    .user(this.user1)
                    .build());
            long start = System.currentTimeMillis();
            mvc.perform(get(CANCEL_TASK_URL + taskId)
                    .with(httpBasic("user1@netgrif.com", "password")));
            long finish = System.currentTimeMillis();
            totalElapsedTime += finish - start;
        }
        log.info("AVG time for event [cancelTask] is [{} ms] for [{}] iterations", totalElapsedTime / iterations, iterations);
    }

    @Test
    public void testFinishAdminPerformance() throws Exception {
        long totalElapsedTime = 0;
        int iterations = 1000;
        LoggedUser loggedUser = superCreatorRunner.getLoggedSuper();

        for (int i = 0; i < iterations; i++) {
            Case useCase = workflowService.createCase(CreateCaseParams.with()
                    .process(net)
                    .author(loggedUser)
                    .locale(Locale.getDefault())
                    .build()).getCase();
            String taskId = useCase.getTasks().stream().findFirst().get().getTask();
            taskService.assignTask(TaskParams.with()
                    .taskId(taskId)
                    .user(loggedUser)
                    .build());
            long start = System.currentTimeMillis();
            mvc.perform(get(FINISH_TASK_URL + taskId)
                    .with(httpBasic("super@netgrif.com", "password")));
            long finish = System.currentTimeMillis();
            totalElapsedTime += finish - start;
        }
        log.info("AVG time for event [finishTask by admin] is [{} ms] for [{}] iterations", totalElapsedTime / iterations, iterations);
    }

    @Test
    public void testFinishPerformance() throws Exception {
        long totalElapsedTime = 0;
        int iterations = 1000;
        LoggedUser loggedUser = superCreatorRunner.getLoggedSuper();

        for (int i = 0; i < iterations; i++) {
            Case useCase = workflowService.createCase(CreateCaseParams.with()
                    .process(net)
                    .author(loggedUser)
                    .locale(Locale.getDefault())
                    .build()).getCase();
            String taskId = useCase.getTasks().stream().findFirst().get().getTask();
            taskService.assignTask(TaskParams.with()
                    .taskId(taskId)
                    .user(this.user1)
                    .build());
            long start = System.currentTimeMillis();
            mvc.perform(get(FINISH_TASK_URL + taskId)
                    .with(httpBasic("user1@netgrif.com", "password")));
            long finish = System.currentTimeMillis();
            totalElapsedTime += finish - start;
        }
        log.info("AVG time for event [finishTask] is [{} ms] for [{}] iterations", totalElapsedTime / iterations, iterations);
    }


}
