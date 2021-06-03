package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetOutcome
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
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

    @Before
    void before() {
        testHelper.truncateDbs();
    }

    @Test
    void testDynamicEnum() {
        ImportPetriNetOutcome optNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/dynamic_choices.xml"), "major", superCreator.getLoggedSuper());

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
