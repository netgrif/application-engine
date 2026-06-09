package com.netgrif.application.engine.filters

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.CaseFilterField
import com.netgrif.application.engine.petrinet.domain.dataset.ProcessFilterField
import com.netgrif.application.engine.petrinet.domain.dataset.TaskFilterField
import com.netgrif.application.engine.petrinet.service.PetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static org.testng.AssertJUnit.assertEquals

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class FilterTest {

    private static final String PROCESS_PATH = "src/test/resources/petriNets/filter_test.xml"

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private UserService userService

    @Autowired
    private IDataService dataService

    @Autowired
    private PetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @BeforeEach
    void beforeEach() {
        testHelper.truncateDbs()
    }

    @Test
    void filterTest() {
        InputStream inputStream = new FileInputStream(PROCESS_PATH)
        PetriNet testProcess = petriNetService.importPetriNet(inputStream, VersionType.MAJOR, superCreator.loggedSuper).getNet()
        inputStream.close()
        assertEquals(CaseFilterField.class, testProcess.getField("filter_field").get().class)
        assertEquals(CaseFilterField.class, testProcess.getField("case_filter_field").get().class)
        assertEquals(TaskFilterField.class, testProcess.getField("task_filter_field").get().class)
        assertEquals(ProcessFilterField.class, testProcess.getField("process_filter_field").get().class)

        Case testCase = workflowService.createCase(testProcess.stringId, "", "", superCreator.loggedSuper).case
        assertEquals("someQueryString", testCase.getFieldValue("filter_field"))
        assertEquals("someQueryString", testCase.getFieldValue("case_filter_field"))
        assertEquals("someQueryString", testCase.getFieldValue("task_filter_field"))
        assertEquals("someQueryString", testCase.getFieldValue("process_filter_field"))

        String taskId = testCase.tasks.find { taskPair -> taskPair.transition == "t1" }.task
        testCase = dataService.setData(taskId, ImportHelper.populateDataset([
                "filter_field": ["type": "filter", "value": "newQueryString"],
                "case_filter_field": ["type": "caseFilter", "value": "newQueryString"],
                "task_filter_field": ["type": "taskFilter", "value": "newQueryString"],
                "process_filter_field": ["type": "processFilter", "value": "newQueryString"],
        ])).case
        assertEquals("newQueryString", testCase.getFieldValue("filter_field"))
        assertEquals("newQueryString", testCase.getFieldValue("case_filter_field"))
        assertEquals("newQueryString", testCase.getFieldValue("task_filter_field"))
        assertEquals("newQueryString", testCase.getFieldValue("process_filter_field"))
    }
}
