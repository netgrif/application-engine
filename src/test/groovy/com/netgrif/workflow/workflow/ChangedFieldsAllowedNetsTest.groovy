package com.netgrif.workflow.workflow

import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldsTree
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class ChangedFieldsAllowedNetsTest {

    private static final String TASK_TITLE = "Task"
    private static final String TRIGGER_FIELD_ID = "text"
    private static final String CASE_REF_FIELD_ID = "caseRef"
    private static final String CASE_REF_FIELD_ID2 = "caseRef2"
    private static final String ALLOWED_NETS_KEY = "allowedNets"
    private static final String NET_IDENTIFIER = "changed_fields_allowed_nets"
    private static final String NET_IDENTIFIER2 = "org_group"

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IDataService dataService

    private PetriNet net

    @BeforeEach
    void beforeAll() {
        def netOptional = importHelper.createNet("changed_fields_allowed_nets.xml")
        assert netOptional.isPresent()
        this.net = netOptional.get()
    }

    // NAE-1374
    @Test
    void changedFieldsAllowedNets() {
        def aCase = importHelper.createCase("Case", this.net)
        assert aCase != null

        def taskId = importHelper.getTaskId(TASK_TITLE, aCase.stringId)
        assert taskId != null

        importHelper.assignTaskToSuper(TASK_TITLE, aCase.stringId)

        ChangedFieldsTree dataSet = dataService.setData(taskId, ImportHelper.populateDataset(
                [(TRIGGER_FIELD_ID): [
                        "value": "trigger",
                        "type" : "text"
                ]]
        ))

        def changeMap = dataSet.getChangedFields()
        assert changeMap.containsKey(CASE_REF_FIELD_ID)
        assert changeMap.get(CASE_REF_FIELD_ID).attributes.containsKey(ALLOWED_NETS_KEY)
        def newAllowedNets = changeMap.get(CASE_REF_FIELD_ID).attributes.get(ALLOWED_NETS_KEY)
        assert newAllowedNets instanceof ArrayList
        assert newAllowedNets.size() == 1
        assert newAllowedNets.get(0) == NET_IDENTIFIER

        def changeContainer = dataSet.flatten()
        assert changeContainer.getChangedFields().containsKey(CASE_REF_FIELD_ID)
        assert changeContainer.getChangedFields().get(CASE_REF_FIELD_ID).containsKey(ALLOWED_NETS_KEY)
        newAllowedNets = changeContainer.getChangedFields().get(CASE_REF_FIELD_ID).get(ALLOWED_NETS_KEY)
        assert newAllowedNets instanceof ArrayList
        assert newAllowedNets.size() == 1
        assert newAllowedNets.get(0) == NET_IDENTIFIER

        assert changeMap.containsKey(CASE_REF_FIELD_ID2)
        assert changeMap.get(CASE_REF_FIELD_ID2).attributes.containsKey(ALLOWED_NETS_KEY)
        newAllowedNets = changeMap.get(CASE_REF_FIELD_ID2).attributes.get(ALLOWED_NETS_KEY)
        assert newAllowedNets instanceof ArrayList
        assert newAllowedNets.size() == 2
        assert newAllowedNets.contains(NET_IDENTIFIER)
        assert newAllowedNets.contains(NET_IDENTIFIER2)

        changeContainer = dataSet.flatten()
        assert changeContainer.getChangedFields().containsKey(CASE_REF_FIELD_ID2)
        assert changeContainer.getChangedFields().get(CASE_REF_FIELD_ID2).containsKey(ALLOWED_NETS_KEY)
        newAllowedNets = changeContainer.getChangedFields().get(CASE_REF_FIELD_ID2).get(ALLOWED_NETS_KEY)
        assert newAllowedNets instanceof ArrayList
        assert newAllowedNets.size() == 2
        assert newAllowedNets.contains(NET_IDENTIFIER)
        assert newAllowedNets.contains(NET_IDENTIFIER2)
    }
}
