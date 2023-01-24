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
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import groovy.transform.CompileStatic
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
@CompileStatic
class CaseFieldTest {

    public static final String ALLOWED_NETS_NET_FILE = "change_allowed_nets_action_test.xml"
    public static final String ALLOWED_NETS_TASK_TITLE = "Tran"

    public static final String CHANGE_VALUE_NET_FILE = "change_caseref_value_action_test.xml"
    public static final String CHANGE_VALUE_TASK_TITLE = "Tran"
    public static final String CASE_FIELD_ID = "caseref"

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

    private Closure<InputStream> stream = { String name ->
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

        Field<?> field = aCase.getDataSet().get(CASE_FIELD_ID)
        assert field instanceof CaseField
        CaseField caseField = field as CaseField
        assert caseField.allowedNets.size() == 1
        assert caseField.allowedNets.get(0) == "lorem"

        importHelper.assignTaskToSuper(ALLOWED_NETS_TASK_TITLE, aCase.stringId)
        SetDataEventOutcome changed1 = importHelper.setTaskData(ALLOWED_NETS_TASK_TITLE, aCase.stringId, new DataSet([
                "setVal": new BooleanField(rawValue: true)
        ] as Map<String,Field<?>>))

        SetDataEventOutcome setDataEventOutcome1 = changed1.outcomes.first() as SetDataEventOutcome
        assert setDataEventOutcome1.changedFields.fields.containsKey(CASE_FIELD_ID)
        List<String> list1 = ((CaseField)setDataEventOutcome1.changedFields.fields[CASE_FIELD_ID]).allowedNets
        assert list1.size() == 2
        assert list1.get(0) == "hello"
        assert list1.get(1) == "world"

        def caseOpt = caseRepository.findById(aCase.stringId)
        assert caseOpt.isPresent()
        aCase = caseOpt.get()

        CaseField caseField1 = aCase.getDataSet().get(CASE_FIELD_ID) as CaseField
        assert caseField1.allowedNets.size() == 2
        assert caseField1.allowedNets.get(0) == "hello"
        assert caseField1.allowedNets.get(1) == "world"

        SetDataEventOutcome changed2 = importHelper.setTaskData(ALLOWED_NETS_TASK_TITLE, aCase.stringId, new DataSet([
                "setNull": new BooleanField(rawValue: true)
        ] as Map<String,Field<?>>))

        SetDataEventOutcome setDataEventOutcome2 = changed2.outcomes.first() as SetDataEventOutcome
        assert setDataEventOutcome2.changedFields.fields.containsKey(CASE_FIELD_ID)
        List<String> list2 = ((CaseField)setDataEventOutcome2.changedFields.fields[CASE_FIELD_ID]).allowedNets
        assert list2.size() == 0

        caseOpt = caseRepository.findById(aCase.stringId)
        assert caseOpt.isPresent()
        aCase = caseOpt.get()
        CaseField caseField2 = aCase.getDataSet().get(CASE_FIELD_ID) as CaseField
        assert caseField2.allowedNets.size() == 0
    }

    @Test
    void testImmediateAllowedNets() {
        def testNet = petriNetService.importPetriNet(stream(ALLOWED_NETS_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert testNet.getNet() != null

        Case aCase = importHelper.createCase("Case 1", testNet.getNet())

        assert aCase.getImmediateData().size() == 1
        CaseField caseRef = (CaseField) aCase.getImmediateData().get(0)
        assert caseRef.allowedNets.size() == 1
        assert caseRef.allowedNets.get(0) == "lorem"

        aCase = workflowService.findAllById([aCase.stringId]).get(0)

        assert aCase.getImmediateData() != null
        assert aCase.getImmediateData().size() == 1
        caseRef = (CaseField) aCase.getImmediateData().get(0)
        assert caseRef.allowedNets.size() == 1
        assert caseRef.allowedNets.get(0) == "lorem"

        importHelper.assignTaskToSuper(ALLOWED_NETS_TASK_TITLE, aCase.stringId)
        importHelper.setTaskData(ALLOWED_NETS_TASK_TITLE, aCase.stringId, new DataSet([
                "setVal": new BooleanField(rawValue: true)
        ] as Map<String,Field<?>>))

        aCase = workflowService.findAllById([aCase.stringId]).get(0)

        assert aCase.getImmediateData() != null
        assert aCase.getImmediateData().size() == 1
        caseRef = (CaseField) aCase.getImmediateData().get(0)
        assert caseRef.allowedNets.size() == 2
        assert caseRef.allowedNets.get(0) == "hello"
        assert caseRef.allowedNets.get(1) == "world"

        SetDataEventOutcome changed2 = importHelper.setTaskData(ALLOWED_NETS_TASK_TITLE, aCase.stringId, new DataSet([
                "setNull": new BooleanField(rawValue: true)
        ] as Map<String,Field<?>>))

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

        assert aCase.getDataSet().get(CASE_FIELD_ID).value == null

        importHelper.assignTaskToSuper(CHANGE_VALUE_TASK_TITLE, aCase.stringId)

        importHelper.setTaskData(CHANGE_VALUE_TASK_TITLE, aCase.stringId, new DataSet([
                "addExisting": new BooleanField(rawValue: true)
        ] as Map<String,Field<?>>))

        def caseOpt = caseRepository.findById(aCase.stringId)
        assert caseOpt.isPresent()
        aCase = caseOpt.get()
        CaseField caseField1 = aCase.getDataSet().get(CASE_FIELD_ID) as CaseField
        assert caseField1.rawValue.size() == 1
        assert caseField1.rawValue.get(0) == aCase.getStringId()

        importHelper.setTaskData(CHANGE_VALUE_TASK_TITLE, aCase.stringId, new DataSet([
                "addNew": new BooleanField(rawValue: true)
        ] as Map<String,Field<?>>))

        caseOpt = caseRepository.findById(aCase.stringId)
        assert caseOpt.isPresent()
        aCase = caseOpt.get()

        CaseField caseField2 = aCase.getDataSet().get(CASE_FIELD_ID) as CaseField
        assert caseField2.rawValue.size() == 2
        assert caseField2.rawValue.get(0) == aCase.getStringId()
        String secondCaseId = caseField2.rawValue.get(1)

        caseOpt = caseRepository.findById(secondCaseId)
        assert caseOpt.isPresent()

        importHelper.setTaskData(CHANGE_VALUE_TASK_TITLE, aCase.stringId, new DataSet([
                "addInvalidNet": new BooleanField(rawValue: true)
        ] as Map<String,Field<?>>))

        caseOpt = caseRepository.findById(aCase.stringId)
        assert caseOpt.isPresent()
        aCase = caseOpt.get()
        CaseField caseField3 = aCase.getDataSet().get(CASE_FIELD_ID) as CaseField
        assert caseField3.rawValue.size() == 2
        assert caseField3.rawValue.get(0) == aCase.getStringId()
        assert caseField3.rawValue.get(1) == secondCaseId
    }
}
