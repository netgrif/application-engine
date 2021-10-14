package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@ActiveProfiles(["test"])
@RunWith(SpringRunner.class)
class DynamicCaseNameTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Before
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testInitValues() {
        ImportPetriNetEventOutcome optNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/dynamic_case_name_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        Case useCase = workflowService.createCase(optNet.getNet().stringId, null, "", superCreator.loggedSuper, Locale.forLanguageTag("sk-SK")).getCase()
        assert useCase.title == "SK text value 6"

        Case useCase2 = workflowService.createCase(optNet.getNet().stringId, null, "", superCreator.loggedSuper, Locale.ENGLISH).getCase()
        assert useCase2.title == "EN text value 6"
    }
}
