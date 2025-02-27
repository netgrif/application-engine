package com.netgrif.application.engine.action

import com.netgrif.auth.service.UserService
import com.netgrif.adapter.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.TestHelper
import com.netgrif.core.auth.domain.Authority
import com.netgrif.core.auth.domain.IUser;
import com.netgrif.core.auth.domain.User
import com.netgrif.core.auth.domain.enums.UserState
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.core.petrinet.domain.PetriNet
import com.netgrif.core.petrinet.domain.VersionType
import com.netgrif.core.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import groovy.json.JsonOutput
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.hateoas.MediaTypes
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.hamcrest.core.StringContains.containsString
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class RemoveActionTest {


    public static final String USER_EMAIL = "test@mail.sk"
    public static final String USER_PASSWORD = "password"

    public static final String ROLE_API = "/api/user/{}/role/assign"

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private MongoTemplate template

    @Autowired
    private UserService userService

    @Autowired
    private ProcessRoleService processRoleService

    @Autowired
    private Importer importer

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreatorRunner superCreator;

    @Autowired
    private TestHelper testHelper

    private MockMvc mvc
    private PetriNet petriNet
    private Authentication auth

    @BeforeEach
    void before() {
        testHelper.truncateDbs()

        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        def net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/removeRole_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert net.getNet() != null

        this.petriNet = net.getNet()

        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])

        importHelper.createUser(new com.netgrif.adapter.auth.domain.User(firstName: "Test", lastName : "Integration", email: USER_EMAIL, password: USER_PASSWORD, state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as ProcessRole[])
    }

    private void cleanDatabases() {
        template.db.drop()
        userService.deleteAllUsers()
        processRoleService.deleteAll()
    }

    @Test
    @Disabled(" GroovyRuntime Could not find matching")
    void addAndRemoveRole() {
        IUser user = userService.findByEmail(USER_EMAIL, null)
        auth = new UsernamePasswordAuthenticationToken("super@netgrif.com", "password")

        String adminRoleId = petriNet.getRoles().find { it.value.name.defaultValue == "admin" }.key

        //Has no role, we assign role admin
        def content = JsonOutput.toJson([adminRoleId])
        String userId = user.getStringId()

        mvc.perform(post(ROLE_API.replace("{}", userId))
                .accept(MediaTypes.HAL_JSON_VALUE)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Selected roles assigned to user")))

        User updatedUser = userService.findByEmail(USER_EMAIL, null) as User
        Set<ProcessRole> roles = updatedUser.getProcessRoles()

        String managerRoleId = processRoleService.findAllByDefaultName("manager")?.first()?.stringId

        assert roles.find { it.getStringId() == adminRoleId }
        assert roles.find { it.getStringId() == managerRoleId }

        //On frontend user had two roles admin and manage, and admin was removed, so now to the backend
        //only manager role came, and as part of admin action, this one should get removed inside action
        content = JsonOutput.toJson([managerRoleId])

        mvc.perform(post(ROLE_API.replace("{}", userId))
                .accept(MediaTypes.HAL_JSON_VALUE)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Selected roles assigned to user")))

        updatedUser = userService.findByEmail(USER_EMAIL, null) as User
        roles = updatedUser.getProcessRoles()

        Assert.assertNull(roles.find { it.stringId == adminRoleId })
        Assert.assertNotNull(roles.find { it.stringId == managerRoleId })
    }
}
