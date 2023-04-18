package com.netgrif.application.engine.ipc

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.workflow.domain.Case
import groovy.transform.CompileStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@CompileStatic
class AssignCancelFinishWithCaseTest extends EngineTest {

    private boolean initialised = false

    @BeforeEach
    void setup() {
        if (!initialised) {
            truncateDbs()
            initialised = true
        }
    }

    public static final String ASSIGN_CANCEL_FINISH_NET_FILE = "assign_cancel_finish_with_Case_test.xml"

    @Test
    void testAssignCancelFinish() {
        def testNet = petriNetService.importPetriNet(stream(ASSIGN_CANCEL_FINISH_NET_FILE) as InputStream, VersionType.MAJOR, superCreator.getLoggedSuper())

        assert testNet.getNet() != null

        Case aCase = importHelper.createCase("Case 1", testNet.getNet())
        importHelper.assignTaskToSuper("Task", aCase.stringId)

        def cases = caseRepository.findAllByProcessIdentifier(testNet.getNet().identifier)
        assert cases.find { it.title == "Case 2" }.dataSet.get("field").rawValue == 1
    }
}