package com.netgrif.workflow.integration.dashboard

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class DashboardServiceTest {

    @Autowired
    IWorkflowService workflowService

    @Autowired
    ImportHelper helper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private TestHelper testHelper

    String[] testData = ["dummy", "prod", "dev", "pre-prod", "helper"]
    int[] testDataInt = [15, 20, 32, 11, 7, 12]

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
    }

    @Test
    void dashboardIntegerTest() {
        PetriNet net1 = helper.createNet("all_data.xml", VersionType.MAJOR).get()
        Random random = new Random()
        (1..30).each {
            Case aCase = helper.createCase("Default title", net1)
            aCase.dataSet["number"].value = testDataInt[random.nextInt(testDataInt.length - 1)]
            workflowService.save(aCase)
        }
    }

    @Test
    void dashboardStringTest() {
        PetriNet net1 = helper.createNet("all_data.xml", VersionType.MAJOR).get()
        Random random = new Random()
        (1..30).each {
            Case aCase = helper.createCase("Default title", net1)
            aCase.dataSet["text"].value = testData[random.nextInt(testData.length - 1)]
            workflowService.save(aCase)
        }
    }
}
