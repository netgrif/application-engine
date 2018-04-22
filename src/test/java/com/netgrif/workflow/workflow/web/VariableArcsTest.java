package com.netgrif.workflow.workflow.web;

import com.netgrif.workflow.MockService;
import com.netgrif.workflow.importer.service.Importer;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.arcs.Arc;
import com.netgrif.workflow.petrinet.domain.arcs.VariableArc;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.startup.DefaultRoleRunner;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
public class VariableArcsTest {

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

    @Before
    public void before() {
        repository.deleteAll();
        if (roleRepository.findByName_DefaultValue(ProcessRole.DEFAULT_ROLE) == null) {
            try {
                defaultRoleRunner.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void importTest() throws TransitionNotExecutableException {
        Optional<PetriNet> optionalNet = importer.importPetriNet(new File(NET_PATH), NET_TITLE, NET_INITIALS, new HashMap<>());

        assert optionalNet.isPresent();
        PetriNet net = optionalNet.get();
        PetriNet loaded = service.getPetriNet(net.getStringId());

        List<Arc> arcs = loaded.getArcs().values().stream().flatMap(List::stream).collect(Collectors.toList());
        assert arcs.size() > 0;
        arcs.forEach(arc -> {
            assert ((VariableArc) arc).getFieldId() != null;
        });

        Case useCase = workflowService.createCase(net.getStringId(), "VARTEST", "red", mock.mockLoggedUser());

        assert useCase.getPetriNet().getArcs()
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(arc -> arc instanceof VariableArc)
                .allMatch(arc -> ((VariableArc) arc).getField() != null);

        Page<Task> tasks = taskService.findByCases(new PageRequest(0, 10), Collections.singletonList(useCase.getStringId()));
        assert tasks.getContent() != null && tasks.getContent().size() > 0;

        Task task = tasks.getContent().get(0);
        taskService.assignTask(mock.mockLoggedUser(), task.getStringId());
        taskService.finishTask(mock.mockLoggedUser(), task.getStringId());

        useCase = workflowService.findOne(useCase.getStringId());
        Map<String, Integer> activePlaces = useCase.getActivePlaces();

        int sum = activePlaces.values().stream().mapToInt(Integer::intValue).sum();
        assert sum == 7;
    }
}