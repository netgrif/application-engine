package com.netgrif.workflow.petrinet.webprocessRolesAndPermissionses

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.User

import com.netgrif.workflow.auth.domain.UserState
import com.netgrif.workflow.ipc.TaskApiTest
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator

//import com.netgrif.workflow.orgstructure.domain.Group

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class PetriNetControllerTest {

    public static final String NET_FILE = "process_delete_test.xml"

    private static final String DELETE_PROCESS_URL = "/api/petrinet/"

    private static final String USER_EMAIL = "user@test.com"
    private static final String ADMIN_EMAIL = "admin@test.com"

    private MockMvc mvc

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private TestHelper testHelper

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @BeforeEach
    void before() {
        testHelper.truncateDbs()

        def net = petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert net.isPresent()

        this.net = net.get()

        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])

        importHelper.createUser(new User(name: "Role", surname: "User", email: USER_EMAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
//                [] as Group[],
                [] as ProcessRole[])

        userAuth = new UsernamePasswordAuthenticationToken(USER_EMAIL, "password")

        importHelper.createUser(new User(name: "Admin", surname: "User", email: ADMIN_EMAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("admin")] as Authority[],
//                [] as Group[],
                [] as ProcessRole[])

        adminAuth = new UsernamePasswordAuthenticationToken(ADMIN_EMAIL, "password")
    }

    private PetriNet net

    private Authentication userAuth
    private Authentication adminAuth

    @Test
    void testDeleteProcess() {
        mvc.perform(delete(DELETE_PROCESS_URL + net.stringId)
                .with(authentication(this.userAuth)))
                .andExpect(status().isForbidden())

        mvc.perform(delete(DELETE_PROCESS_URL + net.stringId)
                .with(authentication(this.adminAuth)))
                .andExpect(status().isOk())
    }
}
