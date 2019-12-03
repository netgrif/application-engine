package com.netgrif.workflow.workflow.web;

import com.netgrif.workflow.MockService;
import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.UserProcessRole;
import com.netgrif.workflow.auth.domain.UserState;
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService;
import com.netgrif.workflow.importer.service.Config;
import com.netgrif.workflow.importer.service.Importer;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.arcs.Arc;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.startup.DefaultRoleRunner;
import com.netgrif.workflow.startup.ImportHelper;
import com.netgrif.workflow.startup.SystemUserRunner;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
public class VariableArcsTest {

    public static final Logger log = LoggerFactory.getLogger(VariableArcsTest.class);

    private static final String NET_TITLE = "Variable Arc Test";
    private static final String NET_PATH = "src/test/resources/variable_arc_test.xml";
    public static final String NET_INITIALS = "VAR";

    @Autowired
    private Importer importer;

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
    }

    @Test
    public void importTest() throws TransitionNotExecutableException {
        Optional<PetriNet> optionalNet = importer.importPetriNet(new File(NET_PATH), NET_TITLE, NET_INITIALS, new Config());

        assert optionalNet.isPresent();
        PetriNet net = optionalNet.get();
        PetriNet loaded = service.getPetriNet(net.getStringId());
        User user = new User();
        user.setName("Test");
        user.setSurname("Test");
        user.setPassword("password");
        user.setState(UserState.ACTIVE);
        user.setEmail("VariableArcsTest@test.com");
        user = importHelper.createUser(user,
                new Authority[]{authorityService.getOrCreate(Authority.user)},
                new com.netgrif.workflow.orgstructure.domain.Group[]{importHelper.createGroup("VariableArcsTest")},
                new UserProcessRole[]{});

        List<Arc> arcs = loaded.getArcs().values().stream().flatMap(List::stream).collect(Collectors.toList());
        assert arcs.size() > 0;
//        arcs.forEach(arc -> {
//            assert arc.getReference() != null;
//        });

        Case useCase = workflowService.createCase(net.getStringId(), "VARTEST", "red", mock.mockLoggedUser());

//        assert useCase.getPetriNet().getArcs()
//                .values()
//                .stream()
//                .flatMap(List::stream)
//                .filter(arc -> arc instanceof VariableArc)
//                .allMatch(arc -> ((VariableArc) arc).getField() != null);

        Page<Task> tasks = taskService.findByCases(new PageRequest(0, 10), Collections.singletonList(useCase.getStringId()));
        assert tasks.getContent() != null && tasks.getContent().size() > 0;

        Task task = tasks.getContent().get(0);
        taskService.assignTask(user.transformToLoggedUser(), task.getStringId());
        taskService.finishTask(user.transformToLoggedUser(), task.getStringId());

        useCase = workflowService.findOne(useCase.getStringId());
        Map<String, Integer> activePlaces = useCase.getActivePlaces();

        int sum = activePlaces.values().stream().mapToInt(Integer::intValue).sum();
        assert sum == 7;
    }
}