package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.utils.FullPageRequest
import com.netgrif.application.engine.workflow.domain.Task
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

import java.util.stream.Collectors

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class DynamicEnumerationTest {

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
    public void before() {
        testHelper.truncateDbs();
    }

    @Test
    void testDynamicEnum() {
        ImportPetriNetEventOutcome optNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/test_autocomplete_dynamic.xml"), "major", superCreator.getLoggedSuper());

        assert optNet.getNet() != null;
        def net = optNet.getNet()

        def aCase = importHelper.createCase("Case", net)
        assert aCase != null

        Task task = taskService.findByCases(new FullPageRequest(), Collections.singletonList(aCase.getStringId())).stream().collect(Collectors.toList()).get(0);
        importHelper.assignTask("Autocomplete", aCase.getStringId(), superCreator.getLoggedSuper())

        dataService.setData(task.stringId, ImportHelper.populateDataset([
                "autocomplete": [
                        "value": "Case",
                        "type" : "enumeration"
                ]
        ]))

        def caseOpt = caseRepository.findById(aCase.stringId)
        assert caseOpt.isPresent()
        aCase = caseOpt.get()

        def field = aCase.dataSet["autocomplete"]
        assert field.choices.size() == 1
        assert field.choices.find { it.defaultValue == "Case" }
    }
}
