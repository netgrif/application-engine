package com.netgrif.workflow.elastic

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.WorkflowManagementSystemApplication
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.domain.QCase
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.importer.service.Importer
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
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
    private static final String USER_EMAIL = "super@netgrif.com"
    private static final String USER_PASSW = "password"
    private static final String BASE_URL = "/api/petrinet/"
    private static final int TEST_NET_COUNT = 5000

    @Autowired
    private Importer importer

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private TestHelper testHelper

    @Autowired
    private CaseRepository caseRepository

    private Authentication auth
    private MockMvc mvc
    private String netId

    @Before
    void before() {
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()
        auth = new UsernamePasswordAuthenticationToken(USER_EMAIL, USER_PASSW)

        testHelper.truncateDbs()

        def net = importer.importPetriNet(new File("src/test/resources/all_data.xml"))
        assert net.isPresent()

        netId = net.get().getStringId()

        TEST_NET_COUNT.times {
            importHelper.createCase("Case ${it}", net.get())
        }
    }

    // NAE-1324
    void testDeletePetriNetWithManyInstances() {
        assert caseRepository.count(QCase.case$.processIdentifier.eq("all_data")) == TEST_NET_COUNT
        mvc.perform(
                delete(BASE_URL + netId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .locale(Locale.forLanguageTag(LOCALE_SK))
                        .with(csrf().asHeader())
                        .with(authentication(this.auth))
        )
                .andExpect(status().isOk())
                .andReturn()
    }

}
