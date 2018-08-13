package com.netgrif.workflow.workflow

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.WorkflowManagementSystemApplication
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.domain.Case
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.hamcrest.core.StringContains.containsString
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
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
    private Importer importer

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
                        type   : "date",
                        "value": "2018-05-13"
                ]
        ])
    }

    PetriNet getNet() {
        def netOptional = importer.importPetriNet(CaseSearchTest.getClassLoader().getResourceAsStream("case_search_test.xml"), "Case search test", "CST")
        assert netOptional.isPresent()
        return netOptional.get()
    }

    @Test
    void searchByAuthorEmail() {
        performSearch("super@netgrif.com")
    }

    @Test
    void searchByNumberField() {
        performSearch("25")
    }

    @Test
    void searchByTextField() {
        performSearch("Bratislava")
    }

    @Test
    void searchByMoreValues() {
        performSearch("Prdel")
    }

    @Test
    void searchByDate() {
        performSearch("2018-05-13")
    }


    void performSearch(String input) {
        mvc.perform(get("/api/workflow/case/fulltext")
                .param("process", "net")
                .param("search", input)
                .with(httpBasic("super@netgrif.com", userPassword)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json;charset=UTF-8"))
                .andExpect(content().string(containsString("_links")))
                .andExpect(content().string(containsString("cases")))
                .andExpect(content().string(containsString(input)))
                .andReturn()
    }


}
