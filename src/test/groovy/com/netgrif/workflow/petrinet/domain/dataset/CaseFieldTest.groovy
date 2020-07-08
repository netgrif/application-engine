package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.ipc.TaskApiTest
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
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
class CaseFieldTest {

    public static final String ALLOWED_NETS_NET_FILE = "change_allowed_nets_action_test.xml"
    public static final String ALLOWED_NETS_TASK_TITLE = "Tran"

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

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @Before
    void setup() {
        testHelper.truncateDbs()
    }

    @Test
    void testAllowedNets() {
        def testNet = petriNetService.importPetriNet(stream(ALLOWED_NETS_NET_FILE), "major", superCreator.getLoggedSuper())
        assert testNet.isPresent()

        Case aCase = importHelper.createCase("Case 1", testNet.get())

        assert aCase.getField("caseref") instanceof CaseField
        assert ((CaseField) aCase.getField("caseref")).allowedNets.size() == 1
        assert ((CaseField) aCase.getField("caseref")).allowedNets.get(0) == "lorem"

        importHelper.assignTaskToSuper(ALLOWED_NETS_TASK_TITLE, aCase.stringId)
        ChangedFieldContainer changed1 = importHelper.setTaskData(ALLOWED_NETS_TASK_TITLE, aCase.stringId, [
                "setVal": [
                        "value": true,
                        "type": importHelper.FIELD_BOOLEAN
                ]
        ])

        assert changed1.getChangedFields().containsKey("caseref")
        assert changed1.getChangedFields().get("caseref").containsKey("allowedNets")
        assert changed1.getChangedFields().get("caseref").get("allowedNets") instanceof List
        List<String> list1 = (List<String>) changed1.getChangedFields().get("caseref").get("allowedNets")
        assert list1.size() == 2
        assert list1.get(0) == "hello"
        assert list1.get(1) == "world"

        def caseOpt = caseRepository.findById(aCase.stringId)
        assert caseOpt.isPresent()
        aCase = caseOpt.get()
        assert aCase.getDataSet().get("caseref").allowedNets.size() == 2
        assert aCase.getDataSet().get("caseref").allowedNets.get(0) == "hello"
        assert aCase.getDataSet().get("caseref").allowedNets.get(1) == "world"

        ChangedFieldContainer changed2 = importHelper.setTaskData(ALLOWED_NETS_TASK_TITLE, aCase.stringId, [
                "setNull": [
                        "value": true,
                        "type": importHelper.FIELD_BOOLEAN
                ]
        ])

        assert changed2.getChangedFields().containsKey("caseref")
        assert changed2.getChangedFields().get("caseref").containsKey("allowedNets")
        assert changed2.getChangedFields().get("caseref").get("allowedNets") instanceof List
        List<String> list2 = (List<String>) changed2.getChangedFields().get("caseref").get("allowedNets")
        assert list2.size() == 0

        caseOpt = caseRepository.findById(aCase.stringId)
        assert caseOpt.isPresent()
        aCase = caseOpt.get()
        assert aCase.getDataSet().get("caseref").allowedNets.size() == 0

    }
}
