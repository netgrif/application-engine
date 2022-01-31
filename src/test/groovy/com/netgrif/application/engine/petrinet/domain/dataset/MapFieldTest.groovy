package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.TestHelper
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
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class MapFieldTest {

    @Autowired
    private IPetriNetService petriNetService
    @Autowired
    private SuperCreator superCreator
    @Autowired
    private TestHelper testHelper
    @Autowired
    private ImportHelper importHelper
    @Autowired
    private IWorkflowService workflowService

    @Value("classpath:data_map.xml")
    private Resource netResource

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testImport() {
        def netOptional = petriNetService.importPetriNet(netResource.inputStream, VersionType.MAJOR, superCreator.loggedSuper)
        assert netOptional.getNet() != null

        def net = netOptional.getNet()
        assert net.dataSet.size() == 1

        EnumerationMapField field = net.dataSet["enumeration"] as EnumerationMapField
        assert field != null
        assert field.name.defaultValue == "Enumeration map"
        assert field.options.size() == 3
        assert field.options["first"].defaultValue == "First option"
        assert field.options["first"].getTranslation("sk") == "Prvá možnosť"
        assert field.options["first"].getTranslation("de") == "Erste Option"
        assert field.options["second"].defaultValue == "Second option"
        assert field.options["second"].getTranslation("sk") == "Druhá možnosť"
        assert field.options["second"].getTranslation("de") == "Zweite Option"
        assert field.options["third"].defaultValue == "Third option"
        assert field.options["third"].getTranslation("sk") == "Tretia možnosť"
        assert field.options["third"].getTranslation("de") == "Dritte Option"
        assert field.defaultValue == "second"
    }

    @Test
    void testValue() {
        def netOptional = petriNetService.importPetriNet(netResource.inputStream, VersionType.MAJOR, superCreator.loggedSuper)
        assert netOptional.getNet() != null

        Case aCase = importHelper.createCase("Case", netOptional.getNet())

        assert aCase.dataSet["enumeration"] != null
        assert aCase.dataSet["enumeration"].value == "second"

        Field field = aCase.immediateData.find { f -> f.stringId == "enumeration" }

        assert field != null

        EnumerationMapField enumMap = (EnumerationMapField) field

        assert enumMap.value == "second"

        List<Case> cases = workflowService.findAllById([aCase.stringId])

        assert cases.size() == 1

        aCase = cases[0]

        field = aCase.immediateData.find { f -> f.stringId == "enumeration" }

        assert field != null

        enumMap = (EnumerationMapField) field

        assert enumMap.value == "second"
    }


    @Value("classpath:data_map_2.xml")
    private Resource netResource2

    @Test
    void testImportMultichoice() {
        def netOptional = petriNetService.importPetriNet(netResource2.inputStream, VersionType.MAJOR, superCreator.loggedSuper)
        assert netOptional.getNet() != null

        def net = netOptional.getNet()
        assert net.dataSet.size() == 1

        MultichoiceMapField field = net.dataSet["multichoice"] as MultichoiceMapField
        assert field.name.defaultValue == "Multichoice map"
        assert field.options.size() == 3
        assert field.options["first"].defaultValue == "First option"
        assert field.options["first"].getTranslation("sk") == "Prvá možnosť"
        assert field.options["first"].getTranslation("de") == "Erste Option"
        assert field.options["second"].defaultValue == "Second option"
        assert field.options["second"].getTranslation("sk") == "Druhá možnosť"
        assert field.options["second"].getTranslation("de") == "Zweite Option"
        assert field.options["third"].defaultValue == "Third option"
        assert field.options["third"].getTranslation("sk") == "Tretia možnosť"
        assert field.options["third"].getTranslation("de") == "Dritte Option"
        assert field.defaultValue.contains("second")
        assert field.defaultValue.contains("first")
    }

    @Test
    void testValueMultichoice() {
        def netOptional = petriNetService.importPetriNet(netResource2.inputStream, VersionType.MAJOR, superCreator.loggedSuper)
        assert netOptional.getNet() != null

        Case aCase = importHelper.createCase("Case", netOptional.getNet())

        assert aCase.dataSet["multichoice"] != null
        assert aCase.dataSet["multichoice"].value.size() == 2
        assert aCase.dataSet["multichoice"].value.find { v -> v == "second" }
        assert aCase.dataSet["multichoice"].value.find { v -> v == "first" }

        Field field = aCase.immediateData.find { f -> f.stringId == "multichoice" }

        assert field != null

        MultichoiceMapField enumMap = (MultichoiceMapField) field

        assert enumMap.value.size() == 2
        assert enumMap.value.find { v -> v == "second" }
        assert enumMap.value.find { v -> v == "first" }

        List<Case> cases = workflowService.findAllById([aCase.stringId])

        assert cases.size() == 1

        aCase = cases[0]

        field = aCase.immediateData.find { f -> f.stringId == "multichoice" }

        assert field != null

        enumMap = (MultichoiceMapField) field

        assert enumMap.value.size() == 2
        assert enumMap.value.find { v -> v == "second" }
        assert enumMap.value.find { v -> v == "first" }
    }
}