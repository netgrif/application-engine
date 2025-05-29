package com.netgrif.application.engine.integration.dashboard

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
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
    private IWorkflowService workflowService

    @Autowired
    private ImportHelper helper

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
        TestHelper.login(superCreator.superIdentity)
    }

    @Test
    void dashboardIntegerTest() {
        // todo: release/8.0.0 file petriNets/all_data.xml is empty
        Process net1 = helper.createNet("all_data.xml", VersionType.MAJOR).get()
        Random random = new Random()
        (1..30).each {
            Case aCase = helper.createCase("Default title", net1)
            aCase.dataSet.get("number").rawValue = testDataInt[random.nextInt(testDataInt.length - 1)]
            workflowService.save(aCase)
        }
    }

    @Test
    void dashboardStringTest() {
        // todo: release/8.0.0 file petriNets/all_data.xml is empty
        Process net1 = helper.createNet("all_data.xml", VersionType.MAJOR).get()
        Random random = new Random()
        (1..30).each {
            Case aCase = helper.createCase("Default title", net1)
            aCase.dataSet.get("text").rawValue = testData[random.nextInt(testData.length - 1)]
            workflowService.save(aCase)
        }
    }
}
