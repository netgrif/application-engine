package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.MockService;
import com.netgrif.application.engine.auth.service.AuthorityService;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.auth.domain.User;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.VersionType;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.runner.DefaultRealmRunner;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
import com.netgrif.application.engine.startup.runner.SystemUserRunner;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import com.netgrif.application.engine.workflow.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.params.TaskParams;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class TaskServiceTest {

    @Autowired
    private ITaskService service;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private PetriNetRepository petriNetRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthorityService authorityService;

    @Autowired
    private SystemUserRunner userRunner;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreatorRunner superCreator;

    @Autowired
    private DefaultRealmRunner realmRunner;

    @Autowired
    private MockService mock;

    @BeforeEach
    public void setUp() throws Exception {
        mongoTemplate.getDb().drop();
        taskRepository.deleteAll();
        realmRunner.run(null);
        userRunner.run(null);

        petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/prikladFM.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreator.getLoggedSuper())
                .build());
        PetriNet net = petriNetRepository.findAll().get(0);
        workflowService.createCase(CreateCaseParams.with()
                .process(net)
                .title("Storage Unit")
                .color("color")
                .author(mock.mockLoggedUser())
                .build());
    }

    @Test
    public void resetArcTest() throws TransitionNotExecutableException, MissingPetriNetMetaDataException, IOException, MissingIconKeyException {
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                    .xmlFile(new FileInputStream("src/test/resources/reset_inhibitor_test.xml"))
                    .releaseType(VersionType.MAJOR)
                    .author(superCreator.getLoggedSuper())
                .build()).getNet();
        LoggedUser loggedUser = mock.mockLoggedUser();
        CreateCaseEventOutcome outcome = workflowService.createCase(CreateCaseParams.with()
                .process(net)
                .title("Reset test")
                .color("color")
                .author(loggedUser)
                .build());
        User user = new User();
        user.setFirstName("name");
        user.setPassword("password");
        user.setLastName("surname");
        user.setEmail("email@email.com");
        user.setState(UserState.ACTIVE);
        user = (User) userService.saveUser(user, null);

        assert outcome.getCase().getConsumedTokens().size() == 0;
        assert outcome.getCase().getActivePlaces().size() == 1;
        assert outcome.getCase().getActivePlaces().values().contains(5);

        Task task = taskRepository.findAll().stream().filter(t -> t.getTitle().getDefaultValue().equalsIgnoreCase("reset")).findFirst().orElse(null);

        assert task != null;

        service.assignTask(TaskParams.with()
                .task(task)
                .user(ActorTransformer.toLoggedUser(user))
                .build());
        Case useCase = caseRepository.findById(outcome.getCase().getStringId()).get();

        assert useCase.getConsumedTokens().size() == 1;
        assert useCase.getConsumedTokens().values().contains(5);
        assert useCase.getActivePlaces().size() == 0;

        service.cancelTask(TaskParams.with()
                .task(task)
                .user(ActorTransformer.toLoggedUser(user))
                .build());
        useCase = caseRepository.findById(useCase.getStringId()).get();

        assert useCase.getConsumedTokens().size() == 0;
        assert useCase.getActivePlaces().size() == 1;
        assert useCase.getActivePlaces().values().contains(5);
    }
}
