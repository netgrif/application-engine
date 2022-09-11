package com.netgrif.application.engine.petrinet.domain

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.repositories.UserRepository
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository
import com.netgrif.application.engine.workflow.service.TaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class ActionRefTest {

    public static final String NET_FILE = "src/test/resources/actionref_test.xml"

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private TaskService taskService

    @Autowired
    private PetriNetRepository netRepository

    @Autowired
    private TaskRepository taskRepository

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private UserRepository userRepository

    @Autowired
    private TestHelper testHelper

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    @Disabled("TODO: deprecated action ref")
    void testEventImport() {

        PetriNet net = petriNetService.importPetriNet(new FileInputStream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()

        assert net.dataSet.get("text_1").events.size() == 8
        assert net.transitions.get("task").dataSet.get("text_1").events.size() == 8
    }
}