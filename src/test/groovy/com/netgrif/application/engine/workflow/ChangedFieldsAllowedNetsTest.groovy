package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
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
    private TestHelper helper

    @Autowired
    private IDataService dataService

    @Autowired
    private TestHelper testHelper

    private PetriNet net

    @BeforeEach
    void beforeAll() {
        helper.truncateDbs()
        def netOptional = importHelper.createNet("changed_fields_allowed_nets.xml")
        assert netOptional.isPresent()
        net = netOptional.get()
    }

    // NAE-1374
    @Test
    void changedFieldsAllowedNets() {
        def aCase = importHelper.createCase("Case", this.net)
        assert aCase != null

        def taskId = importHelper.getTaskId(TASK_TITLE, aCase.stringId)
        assert taskId != null

        importHelper.assignTaskToSuper(TASK_TITLE, aCase.stringId)

        SetDataEventOutcome dataSet = dataService.setData(taskId, ImportHelper.populateDataset(
                [(TRIGGER_FIELD_ID): [
                        "value": "trigger",
                        "type" : "text"
                ]]
        ))

        def changeMap = dataSet.outcomes

        assert dataSet.outcomes.find { SetDataEventOutcome eventOutcome ->
            eventOutcome.getChangedFields().containsKey(CASE_REF_FIELD_ID)
        }
        assert (dataSet.outcomes.find { SetDataEventOutcome eventOutcome ->
            eventOutcome.getChangedFields().containsKey(CASE_REF_FIELD_ID)
        } as SetDataEventOutcome).getChangedFields().get(CASE_REF_FIELD_ID).attributes.containsKey(ALLOWED_NETS_KEY)

        def newAllowedNets = (dataSet.outcomes.find { SetDataEventOutcome eventOutcome ->
            eventOutcome.getChangedFields().containsKey(CASE_REF_FIELD_ID)
        } as SetDataEventOutcome).getChangedFields().get(CASE_REF_FIELD_ID).attributes.get(ALLOWED_NETS_KEY)
        assert newAllowedNets instanceof ArrayList
        assert newAllowedNets.size() == 1
        assert newAllowedNets.get(0) == NET_IDENTIFIER

        assert dataSet.outcomes.find { SetDataEventOutcome eventOutcome ->
            eventOutcome.getChangedFields().containsKey(CASE_REF_FIELD_ID2)
        }
        assert (dataSet.outcomes.find { SetDataEventOutcome eventOutcome ->
            eventOutcome.getChangedFields().containsKey(CASE_REF_FIELD_ID2)
        } as SetDataEventOutcome).getChangedFields().get(CASE_REF_FIELD_ID2).attributes.containsKey(ALLOWED_NETS_KEY)

        newAllowedNets = (dataSet.outcomes.find { SetDataEventOutcome eventOutcome ->
            eventOutcome.getChangedFields().containsKey(CASE_REF_FIELD_ID2)
        } as SetDataEventOutcome).getChangedFields().get(CASE_REF_FIELD_ID2).attributes.get(ALLOWED_NETS_KEY)
        assert newAllowedNets instanceof ArrayList
        assert newAllowedNets.size() == 2
        assert newAllowedNets.contains(NET_IDENTIFIER)
        assert newAllowedNets.contains(NET_IDENTIFIER2)

    }
}
