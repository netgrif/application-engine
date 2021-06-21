package com.netgrif.workflow.petrinet.domain.roles

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.auth.domain.repositories.UserRepository
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.DefaultRoleRunner
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.startup.SystemUserRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class TransitionRoleTest {

    public static final String LIMITS_NET_FILE = "ipc_transition_role.xml"
    public static final String LIMITS_NET_TITLE = "Transition role"
    public static final String LIMITS_NET_INITIALS = "TR"

    @Autowired
    private TestHelper testHelper

    @Autowired
    private Importer importer

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private MongoTemplate template

    @Autowired
    private UserRepository userRepository

    @Autowired
    private ProcessRoleRepository roleRepository

    @Autowired
    private SystemUserRunner systemUserRunner

    @Autowired
    private DefaultRoleRunner roleRunner

    @Autowired
    private ProcessRoleRepository processRoleRepository

    @Autowired
    private IPetriNetService petriNetService;

    private def stream = { String name ->
        return TransitionRoleTest.getClassLoader().getResourceAsStream(name)
    }

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testTaskExecution() {


        def netOptional = petriNetService.importPetriNet(stream(LIMITS_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert netOptional.isPresent()

        def net = netOptional.get()
        assert net.transitions["user_task"].roles.values().size() == 1
        assert processRoleRepository.count() == 2
    }
}