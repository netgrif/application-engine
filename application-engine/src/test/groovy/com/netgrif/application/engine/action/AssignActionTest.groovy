package com.netgrif.application.engine.action

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.objects.auth.domain.AbstractUser
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer
import com.netgrif.application.engine.objects.auth.domain.Authority
import com.netgrif.application.engine.objects.auth.domain.User
import com.netgrif.application.engine.objects.auth.domain.enums.UserState
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import groovy.json.JsonOutput
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class AssignActionTest {

    private static final String GROUP_NAME = "Test group"
    public static final String USER_EMAIL = "test@mail.sk"
    public static final String USER_PASSWORD = "password"

    public static final String ROLE_API = "/api/users/%s/%s/roles"

    @Autowired
    private Importer importer

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private MongoTemplate template

    @Autowired
    private UserService userService

    @Autowired
    private ProcessRoleRepository processRoleRepository

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreatorRunner superCreator

    @Autowired
    private TestHelper testHelper

    private MockMvc mvc
    private PetriNet mainNet
    private PetriNet secondaryNet
    private Authentication authentication
    Map<String, Authority> auths

    @BeforeEach
    void before() {

        testHelper.truncateDbs()

        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        createMainAndSecondaryNet()

        auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])

        importHelper.createUser(new User(firstName: "Test", lastName: "Integration", username: USER_EMAIL, email: USER_EMAIL, password: USER_PASSWORD, state: UserState.ACTIVE),
                [auths.get("user"), auths.get("admin")] as Authority[],
//                [org] as Group[],
                [] as ProcessRole[])
    }

    private void createMainAndSecondaryNet() {
        def mainNet = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/assignRoleMainNet_test_.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreator.getLoggedSuper())
                .build())
        assert mainNet.getNet() != null

        def secondaryNet = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/assignRoleSecondaryNet_test.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreator.getLoggedSuper())
                .build())
        assert secondaryNet.getNet() != null

        this.mainNet = mainNet.getNet()
        this.secondaryNet = secondaryNet.getNet()
    }

    private void cleanDatabases() {
        template.db.drop()
        userService.deleteAllUsers();
        processRoleRepository.deleteAll()
    }

    @Test
    void testAssignRoleOnSecondaryNetWhenRoleIsAddedOnPrimaryNet() {
        AbstractUser user = userService.findByEmail(USER_EMAIL, null)

        authentication = new UsernamePasswordAuthenticationToken(ActorTransformer.toLoggedUser(user), USER_PASSWORD, [auths.get("user"), auths.get("admin")] as List<AuthorityImpl>)
        authentication.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()));

        String roleIdInMainNet = mainNet.getRoles().find { it.value.name.defaultValue == "admin_main" }.key
        String roleIdInSecondaryNetNet = secondaryNet.getRoles().find { it.value.name.defaultValue == "admin_secondary" }.key

        def content = JsonOutput.toJson([roleIdInMainNet, roleIdInSecondaryNetNet])
        String userId = user.getStringId()

        def result = mvc.perform(MockMvcRequestBuilders.put(ROLE_API.formatted(user.getRealmId(),userId))
                .accept(MediaType.APPLICATION_JSON)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf().asHeader())
                .with(authentication(authentication)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()

        AbstractUser updatedUser = userService.findByEmail(USER_EMAIL, null)
        Set<ProcessRole> roles = updatedUser.getProcessRoles()

        String adminMainId = processRoleRepository.findAllByName_DefaultValue("admin_main", Pageable.ofSize(1))?.first()?.stringId
        String adminSecondaryId = processRoleRepository.findAllByName_DefaultValue("admin_secondary", Pageable.ofSize(1))?.first()?.stringId

        assert roles.find { it.stringId == adminMainId }
        assert roles.find { it.stringId == adminSecondaryId }
    }
}
