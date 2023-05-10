package com.netgrif.application.engine.petrinet.service

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.ipc.TaskApiTest
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.utils.FullPageRequest
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.bson.types.ObjectId
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
class PetriNetServiceTest {

    public static final String NET_FILE = "process_delete_test.xml"
    public static final String CUSTOMER_USER_MAIL = "customer@netgrif.com"

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private TestHelper testHelper

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private TaskRepository taskRepository

    @Autowired
    private IProcessRoleService userProcessRoleService

    @Autowired
    private IUserService userService

    @Autowired
    private PetriNetRepository petriNetRepository

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private ProcessRoleRepository processRoleRepository

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        importHelper.createUser(new User(name: "Customer", surname: "User", email: CUSTOMER_USER_MAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as ProcessRole[])
    }

    @Test
    void processDelete() {
        long processRoleCount = processRoleRepository.count()
        long processCount = petriNetRepository.count()
        long taskCount = taskRepository.count()

        ImportPetriNetEventOutcome testNetOptional = petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert testNetOptional.getNet() != null
        assert petriNetRepository.count() == processCount + 1
        PetriNet testNet = testNetOptional.getNet()
        importHelper.createCase("Case 1", testNet)

        assert caseRepository.findAllByProcessIdentifier(testNet.getImportId()).size() == 1
        assert taskRepository.count() == taskCount + 2
        assert processRoleRepository.count() == processRoleCount + 2

        def user = userService.findByEmail(CUSTOMER_USER_MAIL, false)
        assert user != null
        assert user.processRoles.size() == 1

        userService.addRole(user, testNet.roles.values().collect().get(0).stringId)
        user = userService.findByEmail(CUSTOMER_USER_MAIL, false)
        assert user != null
        assert user.processRoles.size() == 2
        assert petriNetService.get(new ObjectId(testNet.stringId)) != null

        Thread.sleep(2000)  //Elastic Index
        petriNetService.deletePetriNet(testNet.stringId, superCreator.getLoggedSuper())
        assert petriNetRepository.count() == processCount
        assert caseRepository.findAllByProcessIdentifier(testNetOptional.getNet().getImportId()).size() == 0
        assert taskRepository.count() == taskCount
        assert processRoleRepository.count() == processRoleCount
        user = userService.findByEmail(CUSTOMER_USER_MAIL, false)
        assert user != null
        assert user.processRoles.size() == 1

        boolean exceptionThrown = false
        try {
            petriNetService.get(new ObjectId(testNet.stringId))
        } catch (IllegalArgumentException e) {
            exceptionThrown = true
            assert e.message.contains(testNet.stringId)
        }
        assert exceptionThrown
    }
}
