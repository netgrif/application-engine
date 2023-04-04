package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import groovy.transform.CompileStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ActiveProfiles(["test"])
@CompileStatic
@ExtendWith(SpringExtension.class)
class DynamicChoicesTest extends EngineTest {

    @BeforeEach
    void before() {
        truncateDbs()
    }

    @Test
    void testDynamicEnum() {
        ImportPetriNetEventOutcome optNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/dynamic_choices.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())

        assert optNet.getNet() != null
        def net = optNet.getNet()

        def aCase = importHelper.createCase("Case", net)

        EnumerationField enumerationField = aCase.dataSet["enumeration"] as EnumerationField
        assert !enumerationField.choices.empty
        assert ["A", "B", "C"].every { str -> enumerationField.choices.any { it.defaultValue == str } }

        EnumerationMapField enumerationMapField = aCase.dataSet["enumeration_map"] as EnumerationMapField
        assert !enumerationMapField.options.isEmpty()
        assert ["a": "A", "b": "B"].values().every { str -> enumerationMapField.options.any { it.value.defaultValue == str } }

        MultichoiceField multichoiceField = aCase.dataSet["multichoice"] as MultichoiceField
        assert !multichoiceField.choices.empty
        assert ["A", "B", "C"].every { str -> multichoiceField.choices.any { it.defaultValue == str } }

        MultichoiceMapField multichoiceMapField = aCase.dataSet["multichoice_map"] as MultichoiceMapField
        assert !multichoiceMapField.options.isEmpty()
        assert ["a": "A", "b": "B"].values().every { str -> multichoiceMapField.options.any { it.value.defaultValue == str } }
    }
}
