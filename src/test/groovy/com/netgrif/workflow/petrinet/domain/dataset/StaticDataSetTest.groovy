package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.domain.arcs.VariableArc
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.Task
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class StaticDataSetTest {

    private static final String FIELD_STATIC_TEXT = "static_text"
    private static final String FIELD_STATIC_FILE = "static_file"
    private static final String FIELD_STATIC_NUMBER = "2000"
    private static final String FIELD_REGULAR_TEXT = "regular_text"
    private static final String FIELD_REGULAR_FILE = "regular_file"
    private static final String FIELD_REGULAR_NUMBER = "3000"

    private static final String FIELD_STATIC_TEXT_INIT = "static value"
    private static final String FIELD_STATIC_NUMBER_INIT = 5
    private static final String FIELD_REGULAR_NUMBER_INIT = 10

    private static final String TRANSITION = "transition"
    private static final String PLACE_2 = "place2"
    private static final String PLACE_3 = "place3"
    private static final String STATIC_VAR_ARC = "a2"
    private static final String VAR_ARC = "a3"

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private PetriNetRepository repository

    @Autowired
    private IDataService dataService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private ITaskService taskService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private TestHelper testHelper

    PetriNet net

    @Before
    void beforeAll() {
        testHelper.truncateDbs()
        def netOpt = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/static_dataset_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert netOpt.isPresent()
        net = netOpt.get()
    }

    @Test
    void testImport() {
        assert net.staticDataSet.containsKey(FIELD_STATIC_TEXT)
        assert net.staticDataSet.containsKey(FIELD_STATIC_FILE)
        assert net.dataSet.containsKey(FIELD_REGULAR_TEXT)
        assert net.dataSet.containsKey(FIELD_REGULAR_FILE)
        assert net.staticDataSet.get(FIELD_STATIC_TEXT).value == FIELD_STATIC_TEXT_INIT
        assert net.staticDataSet.get(FIELD_STATIC_NUMBER).value as Double == FIELD_STATIC_NUMBER_INIT as Double

        VariableArc varArcStatic = net.arcs[TRANSITION].find {it.importId == STATIC_VAR_ARC } as VariableArc
        assert varArcStatic.fieldId == FIELD_STATIC_NUMBER

        VariableArc varArc = net.arcs[TRANSITION].find {it.importId == VAR_ARC } as VariableArc
        assert varArc.fieldId == FIELD_REGULAR_NUMBER

        Case useCase = newCase("Case testImport")

        varArcStatic = useCase.petriNet.arcs[TRANSITION].find {it.importId == STATIC_VAR_ARC } as VariableArc
        assert varArcStatic.field.value as Double == FIELD_STATIC_NUMBER_INIT as Double

        varArc = useCase.petriNet.arcs[TRANSITION].find {it.importId == VAR_ARC } as VariableArc
        assert varArc.field.value as Double == FIELD_REGULAR_NUMBER_INIT as Double
    }

    @Test
    void testDataSetValueSave() {
        net.dataSet.get(FIELD_REGULAR_TEXT).value = "INVALID VALUE"
        net.staticDataSet.get(FIELD_STATIC_TEXT).value = "VALID VALUE"
        net = repository.save(net)
        assert net.dataSet.get(FIELD_REGULAR_TEXT).value == null
        assert net.staticDataSet.get(FIELD_STATIC_TEXT).value == "VALID VALUE"
    }

    @Test
    void testCase() {
        Case useCase = newCase("Case testCase")
        assert !useCase.dataSet.containsKey(FIELD_STATIC_TEXT)
        assert !useCase.dataSet.containsKey(FIELD_STATIC_FILE)
        assert !useCase.dataSet.containsKey(FIELD_STATIC_NUMBER)
    }

    @Test
    void testVarArc() {
        def newStaticNumberValue = 11
        net.staticDataSet[FIELD_STATIC_NUMBER].value = newStaticNumberValue
        net = repository.save(net)

        Case useCase = newCase("Case testVarArc")
        def newRegularNumberValue = 29
        useCase.dataSet[FIELD_REGULAR_NUMBER].value = newRegularNumberValue
        workflowService.save(useCase)

        Task task = taskService.findOne(useCase.tasks.find { it.transition == TRANSITION }.task)
        taskService.assignTask(task, superCreator.superUser)
        taskService.finishTask(task, superCreator.superUser)

        useCase = workflowService.findOne(useCase.stringId)
        assert useCase.activePlaces[PLACE_2] == newStaticNumberValue
        assert useCase.activePlaces[PLACE_3] == newRegularNumberValue
    }

    private Case newCase(String title) {
        return workflowService.createCase(net.stringId, title, "", superCreator.loggedSuper)
    }

}
