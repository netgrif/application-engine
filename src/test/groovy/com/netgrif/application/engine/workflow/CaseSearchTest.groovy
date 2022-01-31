package com.netgrif.application.engine.workflow

import com.fasterxml.jackson.databind.ObjectMapper
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.ApplicationEngine
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.service.CaseSearchService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.hateoas.MediaTypes
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.hamcrest.core.StringContains.containsString
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ApplicationEngine.class
)
@Disabled("searchByMoreValues")
@AutoConfigureMockMvc
class CaseSearchTest {

    @Value('${admin.password:password}')
    private String userPassword

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private TestHelper testHelper

    @Autowired
    private WebApplicationContext wac

    @Autowired
    private SuperCreator superCreator

    private MockMvc mvc

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        PetriNet net = getNet()

        Case case1 = importHelper.createCase("Case1-Test", net)
        Case case2 = importHelper.createCase("Case2", net)
        Case case3 = importHelper.createCase("Case3", net)

        importHelper.assignTaskToSuper("tran", case1.stringId)
        importHelper.assignTaskToSuper("tran", case2.stringId)
        importHelper.assignTaskToSuper("tran", case3.stringId)
        importHelper.setTaskData("tran", case1.stringId, ["1": ["type": "number", "value": "25"]])
        importHelper.setTaskData("tran", case2.stringId, [
                "1": [
                        "type" : "number",
                        "value": "25"
                ],
                "2": [
                        "type" : "text",
                        "value": "Bratislava"
                ]
        ])
        importHelper.setTaskData("tran", case3.stringId, [
                "1": [
                        "type" : "number",
                        "value": "26"
                ],
                "2": [
                        "type" : "text",
                        "value": "Bratislava"
                ],
                "3": [
                        "type" : "text",
                        "value": "Test"
                ],
                "4": [
                        "type" : "date",
                        "value": "12.05.2018"
                ],
                "5": [
                        "type" : "enumeration",
                        "value": "VALUE3"
                ]
        ])

        importHelper.updateSuperUser()
    }

    PetriNet getNet() {
        def netOptional = petriNetService.importPetriNet(
                new FileInputStream("src/test/resources/case_search_test.xml"),
                VersionType.MAJOR,
                superCreator.getLoggedSuper())
        assert netOptional.getNet() != null
        return netOptional.getNet()
    }

    @Test
    void searchByAuthorEmail() {
        performSearch("super@netgrif.com", "Case2")
    }

    @Test
    void searchByNumberField() {
        performSearch("25", "Case2")
    }

    @Test
    void searchByTextField() {
        performSearch("Bratislava", "Case2")
    }

    @Test
    @Disabled("IllegalState")
    void searchByMoreValues() {
        performSearch("Test", "Case1-Test")
    }

    @Test
    void searchByDate() {
        performSearch("12.05.2018", "Case3", false)
    }

    @Test
    void searchByEnum() {
        performSearch("value", "Case3")
    }


    void performSearch(String input, String expect = "", Boolean includeInput = true) {
        String request = buildRequestBody(input)
        mvc.perform(post("/api/workflow/case/search2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(httpBasic("super@netgrif.com", userPassword))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))
                .andExpect(content().string(containsString("_links")))
                .andExpect(content().string(containsString("cases")))
                .andExpect(content().string(containsString(expect)))
                .andExpect(content().string(containsString(includeInput ? input : "")))
                .andReturn()
    }

    String buildRequestBody(String fullText) {
        def map = [
                (CaseSearchService.PETRINET): [
                        (CaseSearchService.PETRINET_IDENTIFIER): "case_search_test.xml"
                ],
                (CaseSearchService.FULLTEXT): fullText
        ]

        ObjectMapper mapper = new ObjectMapper()
        return mapper.writeValueAsString(map)
    }
}