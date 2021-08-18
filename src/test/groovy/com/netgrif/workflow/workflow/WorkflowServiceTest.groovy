package com.netgrif.workflow.workflow

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.ipc.TaskApiTest
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
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
class WorkflowServiceTest {

    public static final String NET_FILE = "case_search_test.xml"
    public static final String CASE_LOCALE_NET_FILE = "create_case_locale.xml"
    public static final String FIRST_AUTO_NET_FILE = "petriNets/NAE_1382_first_trans_auto.xml"

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @Before
    void setup() {
        testHelper.truncateDbs()
    }

    @Test
    void testFindOneImmediateData() {
        def testNet = petriNetService.importPetriNet(stream(NET_FILE), "major", superCreator.getLoggedSuper())
        assert testNet.isPresent()
        Case aCase = importHelper.createCase("Case 1", testNet.get())

        assert aCase.getImmediateData().size() == 5

        def newCase = workflowService.findOne(aCase.stringId)

        assert newCase.getImmediateData() != null
        assert newCase.getImmediateData().size() == 5
    }

    @Test
    void testFirstTransitionAuto() {
        def testNet = petriNetService.importPetriNet(stream(FIRST_AUTO_NET_FILE), "major", superCreator.getLoggedSuper())
        assert testNet.isPresent()

        def net = testNet.get()
        Case aCase = workflowService.createCase(net.stringId, "autoErr", "red", superCreator.getLoggedSuper())
        importHelper.assignTask("Manual", aCase.getStringId(), superCreator.getLoggedSuper())
        importHelper.finishTask("Manual", aCase.getStringId(), superCreator.getLoggedSuper())

        assert workflowService.findOne(aCase.stringId).getActivePlaces().containsKey("p3")
        assert  workflowService.findOne(aCase.stringId).getActivePlaces().size() == 1
    }

    @Test
    void createCaseWithLocale() {
        def testNet = petriNetService.importPetriNet(stream(CASE_LOCALE_NET_FILE), "major", superCreator.getLoggedSuper())
        assert testNet.isPresent()

        def net = testNet.get()
        Case aCase = workflowService.createCase(net.stringId, null, null, superCreator.getLoggedSuper(), new Locale('sk'))

        assert aCase.title.equals("Slovenský preklad")

        Case enCase = workflowService.createCase(net.stringId, null, null, superCreator.getLoggedSuper(), new Locale('en'))

        assert enCase.title.equals("English translation")
    }
}
