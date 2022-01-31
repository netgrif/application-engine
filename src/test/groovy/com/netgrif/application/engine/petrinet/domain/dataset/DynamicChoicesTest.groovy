package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
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
class DynamicChoicesTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreator superCreator;

    @Autowired
    private IDataService dataService;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private CaseRepository caseRepository;

    @BeforeEach
    void before() {
        testHelper.truncateDbs();
    }

    @Test
    void testDynamicEnum() {
        ImportPetriNetEventOutcome optNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/dynamic_choices.xml"), "major", superCreator.getLoggedSuper());

        assert optNet.getNet() != null;
        def net = optNet.getNet()

        def aCase = importHelper.createCase("Case", net)

        assert !aCase.dataSet["enumeration"].choices.empty
        assert ["A", "B", "C"].every { str -> aCase.dataSet["enumeration"].choices.any { it.defaultValue == str } }

        assert !aCase.dataSet["enumeration_map"].options.isEmpty()
        assert ["a": "A", "b": "B"].values().every { str -> aCase.dataSet["enumeration_map"].options.any { it.value.defaultValue == str } }

        assert !aCase.dataSet["multichoice"].choices.empty
        assert ["A", "B", "C"].every { str -> aCase.dataSet["multichoice"].choices.any { it.defaultValue == str } }

        assert !aCase.dataSet["multichoice_map"].options.isEmpty()
        assert ["a": "A", "b": "B"].values().every { str -> aCase.dataSet["multichoice_map"].options.any { it.value.defaultValue == str } }
    }
}
