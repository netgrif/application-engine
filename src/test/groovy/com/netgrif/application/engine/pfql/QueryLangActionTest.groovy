package com.netgrif.application.engine.pfql

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class QueryLangActionTest {

    private static final String TRANS_ID = "t1"
    private static final String QUERY_FIELD_ID = "input_query"
    private static final String RESULT_FIELD_ID = "result"
    private static final String RESULT_CLASS_FIELD_ID = "result_class"
    private static final String SEARCH_GENERAL_SEARCH_FIELD_ID = "general_search"
    private static final String SEARCH_GENERAL_COUNT_FIELD_ID = "general_count"
    private static final String SEARCH_GENERAL_EXISTS_FIELD_ID = "general_exists"

    private static final String SEARCH_ONE_TEMPLATE = "%s_search_one"
    private static final String SEARCH_TEMPLATE = "%s_search"
    private static final String SEARCH_PAGED_TEMPLATE = "%s_paged_search"

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IDataService dataService

    @Autowired
    private SuperCreator superCreator

    private PetriNet testProcess
    private Case testCase
    private String testCaseTaskId

    @BeforeEach
    void beforeEach() {
        testHelper.truncateDbs()
        testProcess = importHelper.createNet("/query_lang_test.xml").get()
        testCase = importHelper.createCase("test", testProcess)
        testCaseTaskId = testCase.tasks.find { taskPair -> taskPair.transition == TRANS_ID }.task
    }

    @Test
    void generalTest() {
        updateQuery("process: identifier == '" + testProcess.identifier + "'")

        pressButton(SEARCH_GENERAL_EXISTS_FIELD_ID)
        assertEquals(true, testCase.getFieldValue(RESULT_FIELD_ID))

        pressButton(SEARCH_GENERAL_COUNT_FIELD_ID)
        assertEquals(1, testCase.getFieldValue(RESULT_FIELD_ID))

        pressButton(SEARCH_GENERAL_SEARCH_FIELD_ID)
        assertEquals("class com.netgrif.application.engine.petrinet.domain.PetriNet", testCase.getFieldValue(RESULT_CLASS_FIELD_ID))
        updateQuery("processes: identifier == '" + testProcess.identifier + "'")
        pressButton(SEARCH_GENERAL_SEARCH_FIELD_ID)
        assertEquals("class java.util.Collections\$UnmodifiableRandomAccessList", testCase.getFieldValue(RESULT_CLASS_FIELD_ID))
    }

    @Test
    void processTest() {
        updateQuery("process: identifier == '" + testProcess.identifier + "'")
        pressButton(String.format(SEARCH_ONE_TEMPLATE, "process"))
        assertEquals("class com.netgrif.application.engine.petrinet.domain.PetriNet", testCase.getFieldValue(RESULT_CLASS_FIELD_ID))

        updateQuery("processes: identifier == '" + testProcess.identifier + "'")
        pressButton(String.format(SEARCH_TEMPLATE, "process"))
        assertEquals("class java.util.Collections\$UnmodifiableRandomAccessList", testCase.getFieldValue(RESULT_CLASS_FIELD_ID))

        pressButton(String.format(SEARCH_PAGED_TEMPLATE, "process"))
        assertEquals("class org.springframework.data.domain.PageImpl", testCase.getFieldValue(RESULT_CLASS_FIELD_ID))

        updateQuery("case: processIdentifier == '" + testProcess.identifier + "'")
        assertThrows(IllegalArgumentException.class, () -> pressButton(String.format(SEARCH_PAGED_TEMPLATE, "process")))
    }

    @Test
    void caseTest() {
        updateQuery("case: processIdentifier == '" + testProcess.identifier + "'")
        pressButton(String.format(SEARCH_ONE_TEMPLATE, "case"))
        assertEquals("class com.netgrif.application.engine.workflow.domain.Case", testCase.getFieldValue(RESULT_CLASS_FIELD_ID))

        updateQuery("cases: processIdentifier == '" + testProcess.identifier + "'")
        pressButton(String.format(SEARCH_TEMPLATE, "case"))
        assertEquals("class java.util.Collections\$UnmodifiableRandomAccessList", testCase.getFieldValue(RESULT_CLASS_FIELD_ID))

        pressButton(String.format(SEARCH_PAGED_TEMPLATE, "case"))
        assertEquals("class org.springframework.data.domain.PageImpl", testCase.getFieldValue(RESULT_CLASS_FIELD_ID))

        updateQuery("process: identifier == '" + testProcess.identifier + "'")
        assertThrows(IllegalArgumentException.class, () -> pressButton(String.format(SEARCH_PAGED_TEMPLATE, "case")))
    }

    @Test
    void taskTest() {
        updateQuery("task: caseId == '" + testCase.stringId + "'")
        pressButton(String.format(SEARCH_ONE_TEMPLATE, "task"))
        assertEquals("class com.netgrif.application.engine.workflow.domain.Task", testCase.getFieldValue(RESULT_CLASS_FIELD_ID))

        updateQuery("tasks: caseId == '" + testCase.stringId + "'")
        pressButton(String.format(SEARCH_TEMPLATE, "task"))
        assertEquals("class java.util.Collections\$UnmodifiableRandomAccessList", testCase.getFieldValue(RESULT_CLASS_FIELD_ID))

        pressButton(String.format(SEARCH_PAGED_TEMPLATE, "task"))
        assertEquals("class org.springframework.data.domain.PageImpl", testCase.getFieldValue(RESULT_CLASS_FIELD_ID))

        updateQuery("process: identifier == '" + testProcess.identifier + "'")
        assertThrows(IllegalArgumentException.class, () -> pressButton(String.format(SEARCH_PAGED_TEMPLATE, "task")))
    }

    @Test
    void userTest() {
        updateQuery("user: email == '" + superCreator.superUser.email + "'")
        pressButton(String.format(SEARCH_ONE_TEMPLATE, "user"))
        assertEquals("class com.netgrif.application.engine.auth.domain.User", testCase.getFieldValue(RESULT_CLASS_FIELD_ID))

        updateQuery("users: email == '" + superCreator.superUser.email + "'")
        pressButton(String.format(SEARCH_TEMPLATE, "user"))
        assertEquals("class java.util.Collections\$UnmodifiableRandomAccessList", testCase.getFieldValue(RESULT_CLASS_FIELD_ID))

        pressButton(String.format(SEARCH_PAGED_TEMPLATE, "user"))
        assertEquals("class org.springframework.data.domain.PageImpl", testCase.getFieldValue(RESULT_CLASS_FIELD_ID))

        updateQuery("process: identifier == '" + testProcess.identifier + "'")
        assertThrows(IllegalArgumentException.class, () -> pressButton(String.format(SEARCH_PAGED_TEMPLATE, "user")))
    }

    private void setData(Map<String, Map<String, Object>> dataSet) {
        testCase = dataService.setData(testCaseTaskId, ImportHelper.populateDataset(dataSet)).case
    }

    private void pressButton(String buttonFieldId) {
        setData([(buttonFieldId): ["value": 0, "type": FieldType.BUTTON.name]])
    }

    private void updateQuery(String query) {
        setData([(QUERY_FIELD_ID): ["value": query, "type": FieldType.TEXT.name]])
    }
}
