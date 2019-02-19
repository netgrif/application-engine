package com.netgrif.workflow.workflow

import com.fasterxml.jackson.databind.ObjectMapper
import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.WorkflowManagementSystemApplication
import com.netgrif.workflow.auth.domain.LoggedUser
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.petrinet.web.requestbodies.UploadedFileMeta
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.domain.Case
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.core.GrantedAuthority
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
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

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = WorkflowManagementSystemApplication.class
)
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

    private MockMvc mvc

    @Before
    void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()
        testHelper.truncateDbs()

        PetriNet net = getNet()

        Case case1 = importHelper.createCase("Case1-Prdel", net)
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
                        "value": "Prdel"
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
                new File("src/test/resources/case_search_test.xml"),
                new UploadedFileMeta("Case search test", "CST", "net", "major"),
                new LoggedUser(1, "super@netgrif.com", "password", new ArrayList<GrantedAuthority>()))
        assert netOptional.isPresent()
        return netOptional.get()
    }

    @Test
    void searchByAuthorEmail() {
        performSearch("super@netgrif.com","Case2")
    }

    @Test
    void searchByNumberField() {
        performSearch("25","Case2")
    }

    @Test
    void searchByTextField() {
        performSearch("Bratislava","Case2")
    }

    @Test
    void searchByMoreValues() {
        performSearch("Prdel","Case1")
    }

    @Test
    void searchByDate() {
        performSearch("12.05.2018","Case3", false)
    }

    @Test
    void searchByEnum(){
        performSearch("value","Case3")
    }


    void performSearch(String input, String expect = "", Boolean includeInput = true) {
        String request = buildRequestBody("net", input)
        mvc.perform(post("/api/workflow/case/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(httpBasic("super@netgrif.com", userPassword))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json;charset=UTF-8"))
                .andExpect(content().string(containsString("_links")))
                .andExpect(content().string(containsString("cases")))
                .andExpect(content().string(containsString(expect)))
                .andExpect(content().string(containsString(includeInput ? input : "")))
                .andReturn()
    }

    String buildRequestBody(String process, String fullText) {
        def map = [
                "petriNet": [
                        "identifier": process
                ],
                "fullText": fullText
        ]

        ObjectMapper mapper = new ObjectMapper()
        return mapper.writeValueAsString(map)
    }
}
