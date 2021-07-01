package com.netgrif.workflow.workflow.web;

import com.netgrif.workflow.MockService;
import com.netgrif.workflow.TestHelper;
import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.UserProcessRole;
import com.netgrif.workflow.auth.domain.UserState;
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService;
import com.netgrif.workflow.importer.service.throwable.MissingIconKeyException;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.arcs.Arc;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.startup.DefaultRoleRunner;
import com.netgrif.workflow.startup.ImportHelper;
import com.netgrif.workflow.startup.SuperCreator;
import com.netgrif.workflow.startup.SystemUserRunner;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import com.netgrif.workflow.workflow.web.responsebodies.TaskReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
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
    private ProcessRoleRepository roleRepository;

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

    private User testUser;

    private Case finishCase;

    private Case cancelCase;

    @Before
    public void before() throws Exception {
        userRunner.run("");
        repository.deleteAll();
        if (roleRepository.findByName_DefaultValue(ProcessRole.DEFAULT_ROLE) == null) {
            try {
                defaultRoleRunner.run();
            } catch (Exception e) {
                log.error("VariableArcsTest failed: ", e);
            }
        }
        testHelper.truncateDbs();
        Optional<PetriNet> optionalNet = service.importPetriNet(new FileInputStream(NET_PATH), "major", superCreator.getLoggedSuper());

        assert optionalNet.isPresent();
        PetriNet net = optionalNet.get();
        this.loaded = service.getPetriNet(net.getStringId());
        User user = new User();
        user.setName("Test");
        user.setSurname("Test");
        user.setPassword("password");
        user.setState(UserState.ACTIVE);
        user.setEmail("VariableArcsTest@test.com");
        testUser = importHelper.createUser(user,
                new Authority[]{authorityService.getOrCreate(Authority.user)},
                new com.netgrif.workflow.orgstructure.domain.Group[]{importHelper.createGroup("VariableArcsTest")},
                new UserProcessRole[]{});

        finishCase = importHelper.createCase("finish case", loaded);
        cancelCase = importHelper.createCase("assign case", loaded);
    }

    @Test
    public void importTest() throws MissingIconKeyException {

        List<Arc> arcs = this.loaded.getArcs().values().stream().flatMap(List::stream).collect(Collectors.toList());
        assert arcs.size() > 0;
        Case useCase = workflowService.createCase(this.loaded.getStringId(), "VARTEST", "red", mock.mockLoggedUser());

        assert useCase.getPetriNet().getArcs()
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(arc -> arc.getReference() != null)
                .allMatch(arc -> arc.getReference().getReferencable() != null);
    }

    @Test
    public void finishTasksTest() {
        List<TaskReference>tasks = taskService.findAllByCase(finishCase.getStringId(), LocaleContextHolder.getLocale());
        assertFinishTasks("regular", tasks);
        assertFinishTasks("reset", tasks);
        assertReadArcsFinishTask(tasks.stream().filter(task -> task.getTitle().contains("read")).collect(Collectors.toList()));
        assertInhibArcsFinishTask(tasks.stream().filter(task -> task.getTitle().contains("inhib")).collect(Collectors.toList()));
    }

    private void assertInhibArcsFinishTask(List<TaskReference> tasks){
        tasks.forEach(taskRef -> {
            Task task = taskService.findOne(taskRef.getStringId());
            try {
                taskService.assignTask(task, testUser);
                finishCase = workflowService.findOne(task.getCaseId());
            } catch (TransitionNotExecutableException e) {
            }
            assert !finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_start") &&
                    !finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_res");
            try {
                taskService.finishTask(task, testUser);
                finishCase = workflowService.findOne(task.getCaseId());
            } catch (TransitionNotExecutableException e) {
            }
            assert !finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_start") &&
                    finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_res");
        });
    }

    private void assertReadArcsFinishTask(List<TaskReference> tasks){
        tasks.forEach(taskRef -> {
            Task task = taskService.findOne(taskRef.getStringId());
            int markingBeforeAssign = 0;
            try {
                markingBeforeAssign = finishCase.getActivePlaces().get(task.getTitle().getDefaultValue() + "_start");
                taskService.assignTask(task, testUser);
                finishCase = workflowService.findOne(task.getCaseId());
            } catch (TransitionNotExecutableException e) {
            }
            assert markingBeforeAssign == finishCase.getActivePlaces().get(task.getTitle().getDefaultValue() + "_start");
            try {
                taskService.finishTask(task, testUser);
                finishCase = workflowService.findOne(task.getCaseId());
            } catch (TransitionNotExecutableException e) {
            }
            assert markingBeforeAssign == finishCase.getActivePlaces().get(task.getTitle().getDefaultValue() + "_start") &&
                    finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_res");
        });
    }

    private void assertFinishTasks(String arcType, List<TaskReference> tasks){
        tasks.stream().filter(task -> task.getTitle().contains(arcType)).forEach(taskRef -> {
            Task task = taskService.findOne(taskRef.getStringId());
            try {
                taskService.assignTask(task, testUser);
                finishCase = workflowService.findOne(task.getCaseId());
            } catch (TransitionNotExecutableException e) {
            }
            assert !finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_start");
            try {
                taskService.finishTask(task, testUser);
                finishCase = workflowService.findOne(task.getCaseId());
            } catch (TransitionNotExecutableException e) {
            }
            assert !finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_start") &&
                    finishCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_res");
        });
    }
    
    @Test
    public void cancelTasksTest(){
        List<TaskReference>tasks = taskService.findAllByCase(cancelCase.getStringId(), LocaleContextHolder.getLocale());
        assertCancelTasks("regular", tasks.stream().filter(task -> task.getTitle().contains("regular")).collect(Collectors.toList()));
        assertCancelTasks("reset", tasks.stream().filter(task -> task.getTitle().contains("reset")).collect(Collectors.toList()));
        assertCancelTasks("read", tasks.stream().filter(task -> task.getTitle().contains("read")).collect(Collectors.toList()));
        assertCancelTasks("inhib", tasks.stream().filter(task -> task.getTitle().contains("inhib")).collect(Collectors.toList()));
    }

    private void assertCancelTasks(String arcType, List<TaskReference> tasks){
        tasks.stream().filter(task -> task.getTitle().contains(arcType)).forEach(taskRef -> {
            Task task = taskService.findOne(taskRef.getStringId());
            int tokensBeforeAssign = 0;
            try {
                if(!arcType.equals("inhib")){
                    tokensBeforeAssign = cancelCase.getActivePlaces().get(task.getTitle().getDefaultValue() + "_start");
                }
                taskService.assignTask(task, testUser);
                cancelCase = workflowService.findOne(task.getCaseId());
            } catch (TransitionNotExecutableException e) {
            }
            assert !cancelCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_res");
            if(arcType.equals("read")){
                assert cancelCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_start");
            } else {
                assert !cancelCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_start");
            }
            int tokensAfterCancel = 0;
            taskService.cancelTask(task, testUser);
            cancelCase = workflowService.findOne(task.getCaseId());
            if(!arcType.equals("inhib")){
                tokensAfterCancel = cancelCase.getActivePlaces().get(task.getTitle().getDefaultValue() + "_start");
            }
            assert tokensBeforeAssign == tokensAfterCancel &&
                    !cancelCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_res");
            if( arcType.equals("inhib")){
                assert !cancelCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_start");
            } else {
                assert cancelCase.getActivePlaces().containsKey(task.getTitle().getDefaultValue() + "_start");
            }
        });
    }

}