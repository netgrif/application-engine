package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.utils.FullPageRequest
import com.netgrif.workflow.workflow.domain.Task
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

import java.util.stream.Collectors

@SpringBootTest
@ActiveProfiles(["test"])
@RunWith(SpringRunner.class)
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

    @Before
    public void before() {
        testHelper.truncateDbs();
    }

    @Test
    void testDynamicEnum() {
        Optional<PetriNet> optNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/test_autocomplete_dynamic.xml"), "major", superCreator.getLoggedSuper());

        assert optNet.isPresent();
        def net = optNet.get()

        def aCase = importHelper.createCase("Case", net)
        assert aCase != null

        Task task = taskService.findByCases(new FullPageRequest(), Collections.singletonList(aCase.getStringId())).stream().collect(Collectors.toList()).get(0);
        importHelper.assignTask("Autocomplete", aCase.getStringId(), superCreator.getLoggedSuper())

        dataService.setData(task.stringId,  ImportHelper.populateDataset([
                "autocomplete": [
                        "value": "Case",
                        "type": "enumeration"
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
