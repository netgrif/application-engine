package com.netgrif.application.engine.workflow.web;

import com.netgrif.application.engine.MockService;
import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.auth.domain.UserState;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.arcs.Arc;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.startup.DefaultRoleRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.SuperCreator;
import com.netgrif.application.engine.startup.SystemUserRunner;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.QTask;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.responsebodies.TaskReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class VariableArcsTest {

    public static final Logger log = LoggerFactory.getLogger(VariableArcsTest.class);
    private static final String NET_PATH = "src/test/resources/variable_arc_test.xml";

    @Autowired
    private PetriNetRepository repository;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IPetriNetService service;

    @Autowired
    private MockService mock;

    @Autowired
    private DefaultRoleRunner defaultRoleRunner;

    @Autowired
    private IProcessRoleService processRoleService;

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private IAuthorityService authorityService;

    @Autowired
    private SystemUserRunner userRunner;

    @Autowired
    private SuperCreator superCreator;

    @Autowired
    private TestHelper testHelper;

    private PetriNet loaded;

    private IUser testUser;

    private Case finishCase;

    private Case cancelCase;

    @BeforeEach
    public void before() throws Exception {
        testHelper.truncateDbs();
        userRunner.run("");
        repository.deleteAll();
        assertNotNull(processRoleService.defaultRole());
        testHelper.truncateDbs();
        ImportPetriNetEventOutcome outcome = service.importPetriNet(new FileInputStream(NET_PATH), "major", superCreator.getLoggedSuper());

        assert outcome.getNet() != null;
        PetriNet net = outcome.getNet();
        this.loaded = service.getPetriNet(net.getStringId());
        User user = new User();
        user.setName("Test");
        user.setSurname("Test");
        user.setPassword("password");
        user.setState(UserState.ACTIVE);
        user.setEmail("VariableArcsTest@test.com");
        testUser = importHelper.createUser(user,
                new Authority[]{authorityService.getOrCreate(Authority.user)},
                new ProcessRole[]{});

        finishCase = importHelper.createCase("finish case", loaded);
        cancelCase = importHelper.createCase("assign case", loaded);
    }

    @Test
    public void importTest() throws MissingIconKeyException {

        List<Arc> arcs = this.loaded.getArcs().values().stream().flatMap(List::stream).collect(Collectors.toList());
        assert arcs.size() > 0;
        CreateCaseEventOutcome caseOutcome = workflowService.createCase(this.loaded.getStringId(), "VARTEST", "red", mock.mockLoggedUser());

        assert caseOutcome.getCase().getPetriNet().getArcs()
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(arc -> arc.getReference() != null)
                .allMatch(arc -> arc.getReference().getReferencable() != null);
    }

    @Test
    public void finishTasksTest() throws TransitionNotExecutableException {
        List<TaskReference> tasks = taskService.findAllByCase(finishCase.getStringId(), LocaleContextHolder.getLocale());
        assertFinishTasks("regular", tasks);
        assertFinishTasks("reset", tasks);
        assertReadArcsFinishTask(tasks.stream().filter(task -> task.getTitle().contains("read")).collect(Collectors.toList()));
        assertInhibArcsFinishTask(tasks.stream().filter(task -> task.getTitle().contains("inhib")).collect(Collectors.toList()));
        assertOutArcsFinishTasks(tasks);
    }

    private void assertInhibArcsFinishTask(List<TaskReference> tasks) throws TransitionNotExecutableException {
        for (TaskReference taskRef : tasks) {
            Task task = taskService.findOne(taskRef.getStringId());
            taskService.assignTask(task, testUser);
            finishCase = workflowService.findOne(task.getCaseId());
            assert !finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_start") &&
                    !finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_res");
            taskService.finishTask(task, testUser);
            finishCase = workflowService.findOne(task.getCaseId());
            assert !finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_start") &&
                    finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_res");
        }
    }

    private void assertReadArcsFinishTask(List<TaskReference> tasks) throws TransitionNotExecutableException {
        for (TaskReference taskRef : tasks) {
            Task task = taskService.findOne(taskRef.getStringId());
            int markingBeforeAssign = 0;
            markingBeforeAssign = finishCase.getActivePlaces().get(task.getTitle().getDefaultValue() + "_start");
            taskService.assignTask(task, testUser);
            finishCase = workflowService.findOne(task.getCaseId());

            assert markingBeforeAssign == finishCase.getActivePlaces().get(task.getTitle().getDefaultValue() + "_start");

            taskService.finishTask(task, testUser);
            finishCase = workflowService.findOne(task.getCaseId());

            assert markingBeforeAssign == finishCase.getActivePlaces().get(task.getTitle().getDefaultValue() + "_start") &&
                    finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_res");
        }
    }

    private void assertFinishTasks(String arcType, List<TaskReference> tasks) throws TransitionNotExecutableException {
        List<TaskReference> filteredTasks = tasks.stream().filter(task -> task.getTitle().contains(arcType)).collect(Collectors.toList());
        for (TaskReference taskRef : filteredTasks) {
            Task task = taskService.findOne(taskRef.getStringId());
            taskService.assignTask(task, testUser);
            finishCase = workflowService.findOne(task.getCaseId());

            assert !finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_start");

            taskService.finishTask(task, testUser);
            finishCase = workflowService.findOne(task.getCaseId());

            assert !finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_start") &&
                    finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_res");
        }
    }

    private void assertOutArcsFinishTasks(List<TaskReference> tasks) throws TransitionNotExecutableException {
        List<TaskReference> filteredTasks = tasks.stream().filter(task -> task.getTitle().equals("var_arc_out") || task.getTitle().equals("place_var_arc_out")).collect(Collectors.toList());
        for (TaskReference taskRef : filteredTasks) {
            Task task = taskService.findOne(taskRef.getStringId());
            taskService.assignTask(task, testUser);
            finishCase = workflowService.findOne(task.getCaseId());

            assert !finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_end");

            taskService.finishTask(task, testUser);
            finishCase = workflowService.findOne(task.getCaseId());

            assert finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_end") &&
                    finishCase.getActivePlaces().get(task.getTitle().getDefaultValue() + "_end") == 2;
        }
    }

    @Test
    public void cancelTasksTest() throws TransitionNotExecutableException {
        List<TaskReference> tasks = taskService.findAllByCase(cancelCase.getStringId(), LocaleContextHolder.getLocale());
        assertCancelTasks("regular", tasks.stream().filter(task -> task.getTitle().contains("regular")).collect(Collectors.toList()));
        tasks = taskService.findAllByCase(cancelCase.getStringId(), LocaleContextHolder.getLocale());
        assertCancelTasks("reset", tasks.stream().filter(task -> task.getTitle().contains("reset")).collect(Collectors.toList()));
        tasks = taskService.findAllByCase(cancelCase.getStringId(), LocaleContextHolder.getLocale());
        assertCancelTasks("read", tasks.stream().filter(task -> task.getTitle().contains("read")).collect(Collectors.toList()));
        tasks = taskService.findAllByCase(cancelCase.getStringId(), LocaleContextHolder.getLocale());
        assertCancelTasks("inhib", tasks.stream().filter(task -> task.getTitle().contains("inhib")).collect(Collectors.toList()));
        tasks = taskService.findAllByCase(cancelCase.getStringId(), LocaleContextHolder.getLocale());
        assertOutArcsCancelTasks(tasks);
    }

    private void assertCancelTasks(String arcType, List<TaskReference> tasks) throws TransitionNotExecutableException {
        List<TaskReference> tasksAfterPlaceRefReset = null;
        for (TaskReference taskRef : tasks) {
            Task task;
            if (tasksAfterPlaceRefReset != null) {
                task = taskService.findOne(tasksAfterPlaceRefReset.stream().filter(taskReference -> taskReference.getTitle().equals(taskRef.getTitle())).findFirst().orElseThrow(() -> new IllegalStateException("Cannot find task reference")).getStringId());
            } else {
                task = taskService.findOne(taskRef.getStringId());
            }
            int dataRefMultiplicityBeforeChange = 0;
            int tokensBeforeAssign = 0;
            if (!arcType.equals("inhib")) {
                tokensBeforeAssign = cancelCase.getActivePlaces().get(task.getTitle().getDefaultValue() + "_start");
            }
            taskService.assignTask(task, testUser);
            cancelCase = workflowService.findOne(task.getCaseId());
            assert !cancelCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_res");
            if (arcType.equals("read")) {
                assert cancelCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_start");
            } else {
                assert !cancelCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_start");
            }
            if (task.getTitle().getDefaultValue().contains("var")) {
                dataRefMultiplicityBeforeChange = (int) Double.parseDouble(cancelCase.getDataSet().get(arcType + "_var").getValue().toString());
                cancelCase.getDataSet().get(arcType + "_var").setValue("800");
                workflowService.save(cancelCase);
            }
            if (task.getTitle().getDefaultValue().contains("ref")) {
                QTask qTask = new QTask("task");
                Task addTokensTask = taskService.searchOne(qTask.transitionId.eq("add_tokens").and(qTask.caseId.eq(cancelCase.getStringId())));
                taskService.assignTask(testUser.transformToLoggedUser(), addTokensTask.getStringId());
                taskService.finishTask(addTokensTask, testUser);
            }
            int tokensAfterCancel = 0;
            taskService.cancelTask(task, testUser);
            cancelCase = workflowService.findOne(task.getCaseId());
            if (!arcType.equals("inhib")) {
                tokensAfterCancel = cancelCase.getActivePlaces().get(task.getTitle().getDefaultValue() + "_start");
            }
            assert tokensBeforeAssign == tokensAfterCancel &&
                    !cancelCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_res");
            if (arcType.equals("inhib")) {
                assert !cancelCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_start");
            } else {
                assert cancelCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_start");
            }

            if (task.getTitle().getDefaultValue().contains("var")) {
                cancelCase.getDataSet().get(arcType + "_var").setValue(dataRefMultiplicityBeforeChange);
                workflowService.save(cancelCase);
            }
            if (task.getTitle().getDefaultValue().contains("ref")) {
                QTask qTask = new QTask("task");
                Task removeTokensTask = taskService.searchOne(qTask.transitionId.eq("remove_tokens").and(qTask.caseId.eq(cancelCase.getStringId())));
                taskService.assignTask(testUser.transformToLoggedUser(), removeTokensTask.getStringId());
                taskService.finishTask(removeTokensTask, testUser);
                tasksAfterPlaceRefReset = taskService.findAllByCase(cancelCase.getStringId(), LocaleContextHolder.getLocale());
            }
        }
    }

    private void assertOutArcsCancelTasks(List<TaskReference> tasks) throws TransitionNotExecutableException {
        List<TaskReference> filteredTasks = tasks.stream().filter(task -> task.getTitle().equals("var_arc_out") || task.getTitle().equals("place_var_arc_out")).collect(Collectors.toList());
        for (TaskReference taskRef : filteredTasks) {
            Task task = taskService.findOne(taskRef.getStringId());
            taskService.assignTask(task, testUser);
            cancelCase = workflowService.findOne(task.getCaseId());

            assert !cancelCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_end");

            taskService.cancelTask(task, testUser);
            cancelCase = workflowService.findOne(task.getCaseId());

            assert !cancelCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_res");
        }
    }

}