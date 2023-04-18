package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import groovy.transform.CompileStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@CompileStatic
@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class DynamicDefaultValueTest extends EngineTest {

    @BeforeEach
    void before() {
        truncateDbs()
    }

    @Test
    void testInitValues() {
        ImportPetriNetEventOutcome optNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/dynamic_init.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        Case useCase = importHelper.createCase("test", optNet.getNet())

        assert useCase.dataSet.get("text").rawValue == superCreator.superUser.name
        assert useCase.dataSet.get("number").rawValue as Integer == superCreator.superUser.name.length()
        assert useCase.dataSet.get("date").rawValue != null
        assert useCase.dataSet.get("dateTime").rawValue != null
        assert (useCase.dataSet.get("user").rawValue as UserFieldValue) != null
        assert (useCase.dataSet.get("multichoice").rawValue as List) == ["ABC", "DEF"]
        assert (useCase.dataSet.get("multichoice_map").rawValue as List) == ["ABC", "DEF"]
    }
}
