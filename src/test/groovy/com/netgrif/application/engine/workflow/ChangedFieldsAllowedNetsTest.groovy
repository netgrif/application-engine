package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import groovy.transform.CompileStatic
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
@CompileStatic
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
//        TODO: release/8.0.0
//        SetDataEventOutcome dataSet = dataService.setData(taskId, ImportHelper.populateDataset(
//                [(TRIGGER_FIELD_ID): [
//                        "value": "trigger",
//                        "type" : "text"
//                ]] as Map<String, Map<String, String>>
//        ))
//
//        def changeMap = dataSet.outcomes
//
//        SetDataEventOutcome outcome = dataSet.outcomes.find { EventOutcome eventOutcome ->
//            if (eventOutcome instanceof SetDataEventOutcome) {
//                eventOutcome.getChangedFields().fields.containsKey(CASE_REF_FIELD_ID)
//            }
//        } as SetDataEventOutcome
//        assert outcome
//        def newAllowedNets = outcome.changedFields.fields.get(CASE_REF_FIELD_ID).allowedNets
//        assert newAllowedNets != null
//        assert newAllowedNets instanceof ArrayList
//        assert newAllowedNets.size() == 1
//        assert newAllowedNets.get(0) == NET_IDENTIFIER
//
//        outcome = dataSet.outcomes.find { EventOutcome eventOutcome ->
//            if (eventOutcome instanceof SetDataEventOutcome) {
//                eventOutcome.getChangedFields().fields.containsKey(CASE_REF_FIELD_ID2)
//            }
//        } as SetDataEventOutcome
//        assert outcome
//        newAllowedNets = outcome.changedFields.fields.get(CASE_REF_FIELD_ID2).allowedNets
//        assert newAllowedNets != null
//        assert newAllowedNets instanceof ArrayList
//        assert newAllowedNets.size() == 1
//        assert newAllowedNets.get(0) == NET_IDENTIFIER2

    }
}
