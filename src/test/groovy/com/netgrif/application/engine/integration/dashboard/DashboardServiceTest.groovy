package com.netgrif.application.engine.integration.dashboard

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.NumberField
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.transform.CompileStatic
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
@CompileStatic
class DashboardServiceTest extends EngineTest {

    String[] testData = ["dummy", "prod", "dev", "pre-prod", "helper"]
    double[] testDataInt = [15d, 20d, 32d, 11d, 7d, 12d]

    @BeforeEach
    void setup() {
        truncateDbs()
    }

    @Test
    void dashboardIntegerTest() {
        PetriNet net1 = importHelper.createNet("all_data.xml", VersionType.MAJOR).get()
        Random random = new Random()
        (1..30).each {
            Case aCase = importHelper.createCase("Default title", net1)
            NumberField numberField = aCase.dataSet.get("number") as NumberField
            numberField.rawValue = testDataInt[random.nextInt(testDataInt.length - 1)]
            workflowService.save(aCase)
        }
    }

    @Test
    void dashboardStringTest() {
        PetriNet net1 = importHelper.createNet("all_data.xml", VersionType.MAJOR).get()
        Random random = new Random()
        (1..30).each {
            Case aCase = importHelper.createCase("Default title", net1)
            (aCase.dataSet.get("text") as TextField).rawValue = testData[random.nextInt(testData.length - 1)]
            workflowService.save(aCase)
        }
    }
}
