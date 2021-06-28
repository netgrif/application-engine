package com.netgrif.workflow.workflow

import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldsTree
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.petrinet.domain.PetriNet
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
class ChangedFieldsAllowedNetsTest {

    private static final String TASK_TITLE = "Task"
    private static final String TRIGGER_FIELD_ID = "text"
    private static final String CASE_REF_FIELD_ID = "caseRef"

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IDataService dataService

    private PetriNet net

    @Before
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

        def changeMap = dataSet.getPropagatedChanges()
        assert changeMap.containsKey(CASE_REF_FIELD_ID)

        def changeContainer = dataSet.flatten()
        assert changeContainer.getChangedFields().containsKey(CASE_REF_FIELD_ID)
    }
}
