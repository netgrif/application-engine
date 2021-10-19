package com.netgrif.workflow.action

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.dataset.Field
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldsTree

import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.domain.Task
import com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import groovy.transform.CompileStatic
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
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
        assert mainNet.getNet() != null
        def $case = importHelper.createCase("Case 1", mainNet.getNet())
        Task task = taskService.findOne($case.tasks.first().task)

        List<Field> dataGet = dataService.getData($case.tasks.first().task).getData()
        dataGet.first().value == ";get-pre;get-post"

        SetDataEventOutcome dataSet = dataService.setData(task.stringId, ImportHelper.populateDataset(
                "text_field": [
                        "value": "",
                        "type" : "text"
                ] as Map
        ))
        assert dataSet.getChangedFields()["control_field"].attributes["value"] == ";get-pre;get-pre;get-post;get-post;set-pre;set-pre;set-post;set-post"
    }
}
