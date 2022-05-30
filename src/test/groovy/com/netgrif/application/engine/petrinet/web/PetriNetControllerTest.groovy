package com.netgrif.application.engine.petrinet.web

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.*
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.ipc.TaskApiTest
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

//import com.netgrif.application.engine.orgstructure.domain.Group

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class PetriNetControllerTest {

    public static final String NET_FILE = "process_delete_test.xml"
    public static final String USER_NET_FILE = "all_data.xml"

    private static final String DELETE_PROCESS_URL = "/api/petrinet/"
    private static final String DELETE_MY_PROCESS_URL = "/api/petrinet/my/"

    private static final String VIEW_MY_PROCESS_URL = "/api/petrinet/my"

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

    @Autowired
    private IUserService userService

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @BeforeEach
    void before() {
        testHelper.truncateDbs()

        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()


        def auths = importHelper.createAuthorities(["user": [AuthorizingObject.FILTER_UPLOAD.name(),
            AuthorizingObject.FILTER_DELETE_MY.name(), AuthorizingObject.PROCESS_DELETE_MY.name(), AuthorizingObject.PROCESS_VIEW_MY.name()],
                                                    "admin": AuthorizingObject.stringValues()])

        IUser basicUser = importHelper.createUser(new User(name: "Role", surname: "User", email: USER_EMAIL, password: "password", state: UserState.ACTIVE),
                auths.get("user").toArray() as Authority[],
//                [] as Group[],
                [] as ProcessRole[])

        userAuth = new UsernamePasswordAuthenticationToken(userService.findByEmail(USER_EMAIL, false).transformToLoggedUser(), "password", auths.get("user"))

        importHelper.createUser(new User(name: "Admin", surname: "User", email: ADMIN_EMAIL, password: "password", state: UserState.ACTIVE),
                auths.get("admin").toArray() as Authority[],
//                [] as Group[],
                [] as ProcessRole[])

        adminAuth = new UsernamePasswordAuthenticationToken(userService.findByEmail(ADMIN_EMAIL, false).transformToLoggedUser(), "password", auths.get("admin"))

        def net = petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert net.getNet() != null
        this.net = net.getNet()

        def userNet = petriNetService.importPetriNet(stream(USER_NET_FILE), VersionType.MAJOR, basicUser.transformToLoggedUser())
        assert userNet.getNet() != null
        this.userNet = userNet.getNet()


    }

    private PetriNet net, userNet

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

    @Test
    void testDeleteMyProcess() {
        mvc.perform(delete(DELETE_MY_PROCESS_URL + userNet.stringId)
                .with(authentication(this.userAuth)))
                .andExpect(status().isOk())
    }

    @Test
    void testViewMyProcess() {
        mvc.perform(get(VIEW_MY_PROCESS_URL)
                .param("identifier", userNet.stringId)
                .with(authentication(this.userAuth)))
                .andExpect(status().isOk())
    }
}
