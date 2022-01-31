package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.ipc.TaskApiTest
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class CaseFieldTest {

    public static final String ALLOWED_NETS_NET_FILE = "change_allowed_nets_action_test.xml"
    public static final String ALLOWED_NETS_TASK_TITLE = "Tran"

    public static final String CHANGE_VALUE_NET_FILE = "change_caseref_value_action_test.xml"
    public static final String CHANGE_VALUE_TASK_TITLE = "Tran"

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private IWorkflowService workflowService

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
    }

    @Test
    void testAllowedNets() {
        def testNet = petriNetService.importPetriNet(stream(ALLOWED_NETS_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert testNet.getNet() != null

        Case aCase = importHelper.createCase("Case 1", testNet.getNet())

        assert aCase.getField("caseref") instanceof CaseField
        assert ((CaseField) aCase.getField("caseref")).allowedNets.size() == 1
        assert ((CaseField) aCase.getField("caseref")).allowedNets.get(0) == "lorem"

        importHelper.assignTaskToSuper(ALLOWED_NETS_TASK_TITLE, aCase.stringId)
        SetDataEventOutcome changed1 = importHelper.setTaskData(ALLOWED_NETS_TASK_TITLE, aCase.stringId, [
                "setVal": [
                        "value": true,
                        "type" : importHelper.FIELD_BOOLEAN
                ]
        ])

        assert (changed1.outcomes[0] as SetDataEventOutcome).getChangedFields().containsKey("caseref")
        assert (changed1.outcomes[0] as SetDataEventOutcome).getChangedFields().get("caseref").attributes.containsKey("allowedNets")
        assert (changed1.outcomes[0] as SetDataEventOutcome).getChangedFields().get("caseref").attributes.get("allowedNets") instanceof List
        List<String> list1 = (List<String>) (changed1.outcomes[0] as SetDataEventOutcome).getChangedFields().get("caseref").attributes.get("allowedNets")
        assert list1.size() == 2
        assert list1.get(0) == "hello"
        assert list1.get(1) == "world"

        def caseOpt = caseRepository.findById(aCase.stringId)
        assert caseOpt.isPresent()
        aCase = caseOpt.get()
        assert aCase.getDataSet().get("caseref").allowedNets.size() == 2
        assert aCase.getDataSet().get("caseref").allowedNets.get(0) == "hello"
        assert aCase.getDataSet().get("caseref").allowedNets.get(1) == "world"

        SetDataEventOutcome changed2 = importHelper.setTaskData(ALLOWED_NETS_TASK_TITLE, aCase.stringId, [
                "setNull": [
                        "value": true,
                        "type" : importHelper.FIELD_BOOLEAN
                ]
        ])

        assert (changed2.outcomes[0] as SetDataEventOutcome).getChangedFields().containsKey("caseref")
        assert (changed2.outcomes[0] as SetDataEventOutcome).getChangedFields().get("caseref").attributes.containsKey("allowedNets")
        assert (changed2.outcomes[0] as SetDataEventOutcome).getChangedFields().get("caseref").attributes.get("allowedNets") instanceof List
        List<String> list2 = (List<String>) (changed2.outcomes[0] as SetDataEventOutcome).getChangedFields().get("caseref").attributes.get("allowedNets")
        assert list2.size() == 0

        caseOpt = caseRepository.findById(aCase.stringId)
        assert caseOpt.isPresent()
        aCase = caseOpt.get()
        assert aCase.getDataSet().get("caseref").allowedNets.size() == 0

    }

    @Test
    void testImmediateAllowedNets() {
        def testNet = petriNetService.importPetriNet(stream(ALLOWED_NETS_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert testNet.getNet() != null

        Case aCase = importHelper.createCase("Case 1", testNet.getNet())

        assert aCase.getImmediateData().size() == 1
        CaseField caseRef = (CaseField) aCase.getImmediateData().get(0)
        assert caseRef.allowedNets.size() == 1
        assert caseRef.allowedNets.get(0).equals("lorem")

        aCase = workflowService.findAllById([aCase.stringId]).get(0)

        assert aCase.getImmediateData() != null
        assert aCase.getImmediateData().size() == 1
        caseRef = (CaseField) aCase.getImmediateData().get(0)
        assert caseRef.allowedNets.size() == 1
        assert caseRef.allowedNets.get(0).equals("lorem")

        importHelper.assignTaskToSuper(ALLOWED_NETS_TASK_TITLE, aCase.stringId)
        importHelper.setTaskData(ALLOWED_NETS_TASK_TITLE, aCase.stringId, [
                "setVal": [
                        "value": true,
                        "type" : importHelper.FIELD_BOOLEAN
                ]
        ])

        aCase = workflowService.findAllById([aCase.stringId]).get(0)

        assert aCase.getImmediateData() != null
        assert aCase.getImmediateData().size() == 1
        caseRef = (CaseField) aCase.getImmediateData().get(0)
        assert caseRef.allowedNets.size() == 2
        assert caseRef.allowedNets.get(0).equals("hello")
        assert caseRef.allowedNets.get(1).equals("world")

        SetDataEventOutcome changed2 = importHelper.setTaskData(ALLOWED_NETS_TASK_TITLE, aCase.stringId, [
                "setNull": [
                        "value": true,
                        "type" : importHelper.FIELD_BOOLEAN
                ]
        ])

        aCase = workflowService.findAllById([aCase.stringId]).get(0)

        assert aCase.getImmediateData() != null
        assert aCase.getImmediateData().size() == 1
        caseRef = (CaseField) aCase.getImmediateData().get(0)
        assert caseRef.allowedNets.size() == 0

    }

    @Test
    @Disabled("Please fix this test")
    void testChangeValueAction() {
        def notAllowedNet = petriNetService.importPetriNet(stream(ALLOWED_NETS_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert notAllowedNet.getNet() != null

        def testNet = petriNetService.importPetriNet(stream(CHANGE_VALUE_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert testNet.getNet() != null

        Case aCase = importHelper.createCase("Case 1", testNet.getNet())

        assert aCase.getDataSet().get("caseref").value == null

        importHelper.assignTaskToSuper(CHANGE_VALUE_TASK_TITLE, aCase.stringId)

        importHelper.setTaskData(CHANGE_VALUE_TASK_TITLE, aCase.stringId, [
                "addExisting": [
                        "value": true,
                        "type" : importHelper.FIELD_BOOLEAN
                ]
        ])

        def caseOpt = caseRepository.findById(aCase.stringId)
        assert caseOpt.isPresent()
        aCase = caseOpt.get()
        assert aCase.getDataSet().get("caseref").value.size() == 1
        assert aCase.getDataSet().get("caseref").value.get(0).equals(aCase.getStringId())

        importHelper.setTaskData(CHANGE_VALUE_TASK_TITLE, aCase.stringId, [
                "addNew": [
                        "value": true,
                        "type" : importHelper.FIELD_BOOLEAN
                ]
        ])

        caseOpt = caseRepository.findById(aCase.stringId)
        assert caseOpt.isPresent()
        aCase = caseOpt.get()
        assert aCase.getDataSet().get("caseref").value.size() == 2
        assert aCase.getDataSet().get("caseref").value.get(0).equals(aCase.getStringId())
        String secondCaseId = aCase.getDataSet().get("caseref").value.get(1)

        caseOpt = caseRepository.findById(secondCaseId)
        assert caseOpt.isPresent()

        importHelper.setTaskData(CHANGE_VALUE_TASK_TITLE, aCase.stringId, [
                "addInvalidNet": [
                        "value": true,
                        "type" : importHelper.FIELD_BOOLEAN
                ]
        ])

        caseOpt = caseRepository.findById(aCase.stringId)
        assert caseOpt.isPresent()
        aCase = caseOpt.get()
        assert aCase.getDataSet().get("caseref").value.size() == 2
        assert aCase.getDataSet().get("caseref").value.get(0).equals(aCase.getStringId())
        assert aCase.getDataSet().get("caseref").value.get(1).equals(secondCaseId)
    }
}
