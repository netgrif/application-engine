package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class DynamicDefaultValueTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testInitValues() {
        ImportPetriNetEventOutcome optNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/dynamic_init.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        Case useCase = importHelper.createCase("test", optNet.getNet())

        assert useCase.dataSet["text"].value == superCreator.superUser.name
        assert useCase.dataSet["number"].value as Integer == superCreator.superUser.name.length()
        assert useCase.dataSet["date"].value != null
        assert useCase.dataSet["dateTime"].value != null
        assert (useCase.dataSet["user"].value as UserFieldValue) != null
        assert (useCase.dataSet["multichoice"].value as List) == ["ABC", "DEF"]
        assert (useCase.dataSet["multichoice_map"].value as List) == ["ABC", "DEF"]
    }
}
