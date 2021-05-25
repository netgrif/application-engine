package com.netgrif.workflow.petrinet.service

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.auth.domain.UserState
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.auth.service.interfaces.IUserProcessRoleService
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.ipc.TaskApiTest
import com.netgrif.workflow.orgstructure.domain.Group
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.utils.FullPageRequest
import com.netgrif.workflow.workflow.domain.repositories.TaskRepository
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
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
    private IUserProcessRoleService userProcessRoleService

    @Autowired
    private IUserService userService

    @Autowired
    private PetriNetRepository petriNetRepository

    @Autowired
    private ProcessRoleRepository processRoleRepository

    @Autowired
    private UserProcessRoleRepository userProcessRoleRepository

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @Before
    void setup() {
        testHelper.truncateDbs()
        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        importHelper.createUser(new User(name: "Customer", surname: "User", email: CUSTOMER_USER_MAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as Group[],
                [] as UserProcessRole[])
    }

    @Test
    void processDelete() {
        long processRoleCount = processRoleRepository.count()
        long userProcessRoleCount = userProcessRoleRepository.count()
        long processCount = petriNetRepository.count()
        int caseCount = workflowService.getAll(new FullPageRequest()).size()
        long taskCount = taskRepository.count()

        Optional<PetriNet> testNetOptional = petriNetService.importPetriNet(stream(NET_FILE), "major", superCreator.getLoggedSuper())
        assert testNetOptional.isPresent()
        assert petriNetRepository.count() == processCount + 1
        PetriNet testNet = testNetOptional.get()
        importHelper.createCase("Case 1", testNet)

        assert workflowService.getAll(new FullPageRequest()).size() == caseCount + 1
        assert taskRepository.count() == taskCount + 2
        assert processRoleRepository.count() == processRoleCount + 2
        assert userProcessRoleRepository.count() == userProcessRoleCount + 2

        def user = userService.findByEmail(CUSTOMER_USER_MAIL, false)
        assert user != null
        assert user.processRoles.size() == 0

        userService.addRole(user, testNet.roles.values().collect().get(0).stringId)
        user = userService.findByEmail(CUSTOMER_USER_MAIL, false)
        assert user != null
        assert user.processRoles.size() == 1



        petriNetService.deletePetriNet(testNet.stringId, superCreator.getLoggedSuper())
        assert petriNetRepository.count() == processCount
        assert workflowService.getAll(new FullPageRequest()).size() == caseCount
        assert taskRepository.count() == taskCount
        assert processRoleRepository.count() == processRoleCount
        assert userProcessRoleRepository.count() == userProcessRoleCount
        user = userService.findByEmail(CUSTOMER_USER_MAIL, false)
        assert user != null
        assert user.processRoles.size() == 0
    }
}
