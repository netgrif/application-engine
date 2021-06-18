package com.netgrif.workflow.elastic

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.WorkflowManagementSystemApplication
import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.auth.domain.UserState
import com.netgrif.workflow.orgstructure.domain.Group
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.QCase
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.importer.service.Importer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.hateoas.MediaTypes
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = WorkflowManagementSystemApplication.class
)
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application-test.properties"
)
class DeletePetriNetWithManyInstancesTest {

    private static final String LOCALE_SK = "sk"
    private static final String TEST_NET = "all_data.xml"
    private static final String BASE_URL = "/api/petrinet/"
    private static final int TEST_NET_COUNT = 5000

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private TestHelper testHelper

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Value("classpath:all_data.xml")
    private Resource petriNetResource

    private Authentication auth
    private MockMvc mvc
    private String netId

    @Before
    void before() {
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        testHelper.truncateDbs()

        def net = importHelper.createNet(TEST_NET, VersionType.MAJOR, superCreator.loggedSuper)
        assert net.isPresent()

        auth = testHelper.getSuperUserAuth()

        netId = net.get().getStringId()

        TEST_NET_COUNT.times {
            importHelper.createCase("Case ${it}", net.get())
        }
    }

    // NAE-1324
    @Test
    void testDeletePetriNetWithManyInstances() {
        assert caseRepository.count(QCase.case$.processIdentifier.eq("all_data")) == TEST_NET_COUNT
        mvc.perform(
                delete(BASE_URL + netId)
                        .accept(MediaTypes.HAL_JSON_VALUE)
                        .locale(Locale.forLanguageTag(LOCALE_SK))
                        .with(csrf().asHeader())
                        .with(authentication(this.auth))
        )
                .andExpect(status().isOk())
                .andDo{result ->
                    assert caseRepository.count(QCase.case$.processIdentifier.eq("all_data")) == 0
                }
    }

}
