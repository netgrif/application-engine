package com.netgrif.workflow.ipc

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetOutcome
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class CaseApiTest {

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private Importer importer

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    private ImportPetriNetOutcome testNet

    private def stream = { String name ->
        return CaseApiTest.getClassLoader().getResourceAsStream(name)
    }

    @Before
    void setup() {
        testHelper.truncateDbs()
    }

    public static final String CREATE_NET_FILE = "ipc_createCase.xml"

    @Test
    void testCreate() {
        testNet = petriNetService.importPetriNet(stream(CREATE_NET_FILE), "major", superCreator.getLoggedSuper())

        assert testNet.getNet() != null

        Case aCase = importHelper.createCase("Case 1", testNet.getNet())
        importHelper.assignTaskToSuper("Task", aCase.stringId)
        importHelper.finishTaskAsSuper("Task", aCase.stringId)

        assert caseRepository.findAllByProcessIdentifier(testNet.getNet().identifier).size() > 1
    }

    public static final String SEARCH_NET_FILE = "ipc_where.xml"

    @Test
    void testSearch() {
        testHelper.truncateDbs()

        testNet = petriNetService.importPetriNet(stream(SEARCH_NET_FILE), "major", superCreator.getLoggedSuper())

        assert testNet.getNet() != null

        List<Case> cases = []
        5.times { index ->
            cases << importHelper.createCase("Case $index" as String, testNet.getNet())
        }

        importHelper.assignTaskToSuper("Task", cases[0].stringId)
        importHelper.finishTaskAsSuper("Task", cases[0].stringId)

        cases = caseRepository.findAll()
        assert cases.find { it.title == "Case 1" }.dataSet["field"].value != 0
        assert cases.findAll { it.title != "Case 1"  && it.processIdentifier == "test" }.every { it.dataSet["field"].value == 0 }
        assert cases.find {it.title == "Case 0"}.dataSet["count"].value == 5
        assert cases.find {it.title == "Case 0"}.dataSet["paged"].value == 1
    }
}