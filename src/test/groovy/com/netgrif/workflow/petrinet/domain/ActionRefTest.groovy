package com.netgrif.workflow.petrinet.domain

import com.netgrif.workflow.TestHelper

import com.netgrif.workflow.auth.domain.repositories.UserRepository
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.DefaultRoleRunner
import com.netgrif.workflow.startup.SuperCreator
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.domain.repositories.TaskRepository
import com.netgrif.workflow.workflow.service.TaskService
import org.junit.jupiter.api.BeforeEach
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
    public static final String NET_FILE = "actionref_test.xml"

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
    private ProcessRoleRepository roleRepository

    @Autowired
    private TestHelper testHelper

    private def stream = { String name ->
        return ActionRefTest.getClassLoader().getResourceAsStream(name)
    }

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    @Ignore // TODO: deprecated action ref
    void testEventImport() {
        testHelper.truncateDbs()

        def net = petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper()).get()

        assert net.dataSet.get("text_1").events.size() == 8
        assert net.transitions.get("task").dataSet.get("text_1").events.size() == 8
    }
}