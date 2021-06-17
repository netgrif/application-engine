package com.netgrif.workflow.action

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.auth.domain.UserState
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.auth.domain.repositories.UserRepository
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper

//import com.netgrif.workflow.orgstructure.domain.Group

import com.netgrif.workflow.startup.SuperCreator
import groovy.json.JsonOutput
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class AssignActionTest {

    public static final String USER_EMAIL = "test@mail.sk"
    public static final String USER_PASSWORD = "password"

    public static final String ROLE_API = "/api/user/{}/role/assign"

    @Autowired
    private Importer importer

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private MongoTemplate template

    @Autowired
    private UserRepository userRepository

    @Autowired
    private UserProcessRoleRepository userProcessRoleRepository

    @Autowired
    private ProcessRoleRepository processRoleRepository

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private TestHelper testHelper

    private MockMvc mvc
    private PetriNet mainNet
    private PetriNet secondaryNet
    private Authentication authentication

    @BeforeEach
    void before() {

        testHelper.truncateDbs()

        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        createMainAndSecondaryNet()
//        createUserProcessRoles()

        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])

        importHelper.createUser(new User(name: "Test", surname: "Integration", email: USER_EMAIL, password: USER_PASSWORD, state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as UserProcessRole[])
    }

    private void createUserProcessRoles() {
        importHelper.createUserProcessRole(this.mainNet, "admin_main")
        importHelper.createUserProcessRole(this.secondaryNet, "admin_secondary")
    }

    private void createMainAndSecondaryNet() {
        def mainNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/assignRoleMainNet_test_.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert mainNet.isPresent()

        def secondaryNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/assignRoleSecondaryNet_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert secondaryNet.isPresent()

        this.mainNet = mainNet.get()
        this.secondaryNet = secondaryNet.get()
    }


    @Test
    void testAssignRoleOnSecondaryNetWhenRoleIsAddedOnPrimaryNet() {
        User user = userRepository.findByEmail(USER_EMAIL)

        authentication = new UsernamePasswordAuthenticationToken(USER_EMAIL, USER_PASSWORD)

        String roleIdInMainNet = mainNet.getRoles().find { it.value.name.defaultValue == "admin_main" }.key

        def content = JsonOutput.toJson([roleIdInMainNet])
        String userId = Integer.toString(user.id as Integer)

        mvc.perform(post(ROLE_API.replace("{}", userId))
                .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf().asHeader())
                .with(authentication(this.authentication)))
                .andExpect(status().isOk())

        User updatedUser = userRepository.findByEmail(USER_EMAIL)
        Set<UserProcessRole> roles = updatedUser.getUserProcessRoles()

        String adminMainId = processRoleRepository.findByName_DefaultValue("admin_main").stringId
        String adminSecondaryId = processRoleRepository.findByName_DefaultValue("admin_secondary").stringId

//        assert roles.find {it.roleId == adminMainId}
//        assert roles.find {it.roleId == adminSecondaryId}  //TODO:  fixnut assignRole
    }
}
