package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.auth.domain.UserState;
import com.netgrif.application.engine.auth.domain.repositories.UserRepository;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.configuration.properties.SuperAdminConfiguration;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.params.ImportPetriNetParams;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.SuperCreator;
import com.netgrif.application.engine.startup.SystemUserRunner;
import com.netgrif.application.engine.startup.UriRunner;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.domain.params.TaskParams;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.bson.types.ObjectId;
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
import java.util.Collections;

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
    private UserRepository userRepository;

    @Autowired
    private IAuthorityService authorityService;

    @Autowired
    private SystemUserRunner userRunner;

    @Autowired
    private UriRunner uriRunner;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreator superCreator;

    @Autowired
    private SuperAdminConfiguration configuration;

    @BeforeEach
    public void setUp() throws Exception {
        mongoTemplate.getDb().drop();
        taskRepository.deleteAll();
        userRunner.run("");
        uriRunner.run();

        petriNetService.importPetriNet(new ImportPetriNetParams(
                new FileInputStream("src/test/resources/prikladFM.xml"), VersionType.MAJOR, superCreator.getLoggedSuper()));
        PetriNet net = petriNetRepository.findAll().get(0);
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .petriNet(net)
                .title("Storage Unit")
                .color("color")
                .loggedUser(mockLoggedUser())
                .build();
        workflowService.createCase(createCaseParams);
    }

    @Test
    public void resetArcTest() throws TransitionNotExecutableException, MissingPetriNetMetaDataException, IOException, MissingIconKeyException {
        PetriNet net = petriNetService.importPetriNet(new ImportPetriNetParams(
                new FileInputStream("src/test/resources/reset_inhibitor_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())).getNet();
        LoggedUser loggedUser = mockLoggedUser();
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .petriNet(net)
                .title("Reset test")
                .color("color")
                .loggedUser(loggedUser)
                .build();
        CreateCaseEventOutcome outcome = workflowService.createCase(createCaseParams);
        User user = new User();
        user.setName("name");
        user.setPassword("password");
        user.setSurname("surname");
        user.setEmail("email@email.com");
        user.setState(UserState.ACTIVE);
        user = userRepository.save(user);

        assert outcome.getCase().getConsumedTokens().isEmpty();
        assert outcome.getCase().getActivePlaces().size() == 1;
        assert outcome.getCase().getActivePlaces().containsValue(5);

        Task task = taskRepository.findAll().stream().filter(t -> t.getTitle().getDefaultValue().equalsIgnoreCase("reset")).findFirst().orElse(null);

        assert task != null;

        service.assignTask(new TaskParams(task, user));
        Case useCase = caseRepository.findById(outcome.getCase().getStringId()).get();

        assert useCase.getConsumedTokens().size() == 1;
        assert useCase.getConsumedTokens().containsValue(5);
        assert useCase.getActivePlaces().isEmpty();

        service.cancelTask(new TaskParams(task, user));
        useCase = caseRepository.findById(useCase.getStringId()).get();

        assert useCase.getConsumedTokens().isEmpty();
        assert useCase.getActivePlaces().size() == 1;
        assert useCase.getActivePlaces().containsValue(5);
    }

    public LoggedUser mockLoggedUser() {
        Authority authorityUser = authorityService.getOrCreate(Authority.user);
        return new LoggedUser(new ObjectId().toString(), configuration.getEmail(), configuration.getPassword(), Collections.singleton(authorityUser));
    }
}