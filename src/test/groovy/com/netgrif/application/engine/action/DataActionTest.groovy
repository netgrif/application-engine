package com.netgrif.application.engine.action

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import groovy.transform.CompileStatic
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
class DataActionTest {

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IDataService dataService

    @Autowired
    private ITaskService taskService

    @Autowired
    private TestHelper testHelper

    @Test
    void testDataActions() {
        testHelper.truncateDbs()

        def mainNet = importHelper.createNet("data_actions_test.xml")
        assert mainNet.isPresent()
        def $case = importHelper.createCase("Case 1", mainNet.get())
        Task task = taskService.findOne($case.tasks.first().task)

        List<Field> dataGet = dataService.getData($case.tasks.first().task).getData()
        dataGet.first().value == ";get-pre;get-post"

        SetDataEventOutcome dataSet = dataService.setData(task.stringId, ImportHelper.populateDataset(
                "text_field": [
                        "value": "",
                        "type" : "text"
                ] as Map
        ))
        assert (dataSet.outcomes.get(dataSet.outcomes.size() - 1) as SetDataEventOutcome)
                .getChangedFields()["control_field"].attributes["value"] == ";get-pre;get-pre;get-post;get-post;set-pre;set-pre;set-post;set-post"
    }
}
