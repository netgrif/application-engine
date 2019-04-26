package com.netgrif.workflow.ipc

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.startup.ImportHelper
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
class AssignCancelFinishWithCaseTest {

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private Importer importer

    @Autowired
    private TestHelper testHelper

    private boolean initialised = false

    private def stream = { String name ->
        return AssignCancelFinishWithCaseTest.getClassLoader().getResourceAsStream(name)
    }

    @Before
    void setup() {
        if (!initialised) {
            testHelper.truncateDbs()
            initialised = true
        }
    }

    public static final String ASSIGN_CANCEL_FINISH_NET_FILE = "assign_cancel_finish_with_Case_test.xml"
    public static final String ASSIGN_CANCEL_FINISH_NET_NAME = "AssignCancelFinish"
    public static final String ASSIGN_CANCEL_FINISH_NET_INITIALS = "ACF"

    @Test
    void testAssignCancelFinish() {
        def testNet = importer.importPetriNet(stream(ASSIGN_CANCEL_FINISH_NET_FILE), ASSIGN_CANCEL_FINISH_NET_NAME, ASSIGN_CANCEL_FINISH_NET_INITIALS)

        assert testNet.isPresent()

        Case aCase = importHelper.createCase("Case 1", testNet.get())
        importHelper.assignTaskToSuper("Task", aCase.stringId)

        def cases = caseRepository.findAllByProcessIdentifier(testNet.get().identifier)
        assert cases.find { it.title == "Case 2" }.dataSet["field"].value == 1
    }
}