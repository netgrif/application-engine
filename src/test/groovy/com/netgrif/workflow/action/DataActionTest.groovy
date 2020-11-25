package com.netgrif.workflow.action

import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.dataset.Field
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.service.interfaces.IDataService
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
    private Importer importer
    @Autowired
    private ImportHelper importHelper
    @Autowired
    private IDataService dataService

    @Test
    void testDataActions() {
        def mainNet = importer.importPetriNet(new File("src/test/resources/data_actions_test.xml"))
        assert mainNet.isPresent()
        def $case = importHelper.createCase("Case 1", mainNet.get())

        List<Field> dataGet = dataService.getData($case.tasks.first().task)
        dataGet.first().value == ";get-pre;get-post"

        ChangedFieldContainer dataSet = dataService.setData($case.tasks.first().task, ImportHelper.populateDataset(
                "text_field": [
                        "value": "",
                        "type" : "text"
                ] as Map
        ))
        dataSet.changedFields["text_field"]["value"] == ";set-pre;set-post"
    }
}
