package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.MockService;
import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.configuration.properties.SuperAdminConfiguration;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.SuperCreator;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
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
    private SuperAdminConfiguration configuration;
    @Autowired
    private TestHelper testHelper;

    @BeforeEach
    public void setUp() throws Exception {
        testHelper.truncateDbs();

        petriNetService.importProcess(new FileInputStream("src/test/resources/prikladFM.xml"), VersionType.MAJOR,
                superCreator.getLoggedSuper().getActiveActorId());
        Process net = petriNetRepository.findAll().get(0);
        workflowService.createCase(net.getStringId(), "Storage Unit", "color", mockService.mockLoggedIdentity().getActiveActorId());
    }

    @Test
    public void resetArcTest() throws TransitionNotExecutableException, MissingPetriNetMetaDataException, IOException, MissingIconKeyException {
        Process net = petriNetService.importProcess(new FileInputStream("src/test/resources/reset_inhibitor_test.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper().getActiveActorId()).getProcess();
        LoggedIdentity mockedLoggedIdentity = mockService.mockLoggedIdentity();
        CreateCaseEventOutcome outcome = workflowService.createCase(net.getStringId(), "Reset test", "color",
                mockedLoggedIdentity.getActiveActorId());

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

        service.assignTask(identity.toSession().getActiveActorId(), task.getStringId());
        Case useCase = caseRepository.findById(outcome.getCase().getStringId()).get();

        assert useCase.getConsumedTokens().size() == 1;
        assert useCase.getConsumedTokens().containsValue(5);
        assert useCase.getActivePlaces().isEmpty();

        service.cancelTask(identity.toSession().getActiveActorId(), task.getStringId());
        useCase = caseRepository.findById(useCase.getStringId()).get();

        assert useCase.getConsumedTokens().isEmpty();
        assert useCase.getActivePlaces().size() == 1;
        assert useCase.getActivePlaces().containsValue(5);
    }
}