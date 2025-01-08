package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.ipc.TaskApiTest
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.workflow.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
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
class WorkflowServiceTest {

    public static final String NET_FILE = "case_search_test.xml"
    public static final String CASE_LOCALE_NET_FILE = "create_case_locale.xml"
    public static final String FIRST_AUTO_NET_FILE = "petriNets/NAE_1382_first_trans_auto.xml"
    public static final String SECOND_AUTO_NET_FILE = "petriNets/NAE_1382_first_trans_auto_2.xml"
    public static final String CHILD_NET_FILE = "importTest/child_extending_parent.xml"
    public static final String PARENT_NET_FILE = "importTest/parent_to_be_extended.xml"
    public static final String SUPER_PARENT_NET_FILE = "importTest/super_parent_to_be_extended.xml"


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

    private Closure<InputStream> stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
    }

    @Test
    void testFindOneImmediateData() {
        def testNet = petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert testNet.getNet() != null
        Case aCase = importHelper.createCase("Case 1", testNet.getNet())

        assert aCase.getImmediateData().size() == 5

        def newCase = workflowService.findOne(aCase.stringId)

        assert newCase.getImmediateData() != null
        assert newCase.getImmediateData().size() == 5
    }

    @Test
    void testFirstTransitionAuto() {
        def testNet = petriNetService.importPetriNet(stream(FIRST_AUTO_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()
        assert testNet

        def net = testNet
        Case aCase = workflowService.createCase(net.stringId, "autoErr", "red", superCreator.getLoggedSuper()).getCase()
        importHelper.assignTask("Manual", aCase.getStringId(), superCreator.getLoggedSuper())
        importHelper.finishTask("Manual", aCase.getStringId(), superCreator.getLoggedSuper())

        assert workflowService.findOne(aCase.stringId).getActivePlaces().containsKey("p3")
        assert workflowService.findOne(aCase.stringId).getActivePlaces().size() == 1
    }

    @Test
    void testSecondTransitionAuto() {
        def net = petriNetService.importPetriNet(stream(SECOND_AUTO_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()

        Case aCase = workflowService.createCase(net.stringId, "autoErr", "red", superCreator.getLoggedSuper()).getCase()
        importHelper.assignTask("Manual", aCase.getStringId(), superCreator.getLoggedSuper())
        importHelper.finishTask("Manual", aCase.getStringId(), superCreator.getLoggedSuper())

        importHelper.assignTask("Manuel", aCase.getStringId(), superCreator.getLoggedSuper())
        importHelper.finishTask("Manuel", aCase.getStringId(), superCreator.getLoggedSuper())

        assert workflowService.findOne(aCase.stringId).getActivePlaces().containsKey("p3")
        assert workflowService.findOne(aCase.stringId).getActivePlaces().size() == 1
    }

    @Test
    void createCaseWithLocale() {
        def testNet = petriNetService.importPetriNet(stream(CASE_LOCALE_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert testNet.getNet() != null

        def net = testNet.getNet()
        Case aCase = workflowService.createCase(net.stringId, null, null, superCreator.getLoggedSuper(), new Locale('sk')).getCase()

        assert aCase.title == "Slovenský preklad"
        // TODO: release/8.0.0 fix uri nodes
//        assert workflowService.findOne(aCase.stringId).uriNodeId == net.uriNodeId

        Case enCase = workflowService.createCase(net.stringId, null, null, superCreator.getLoggedSuper(), new Locale('en')).getCase()

        assert enCase.title == "English translation"
    }

    @Test
    void createCaseOfExtendedPetriNet() {
        Process superParentNet = petriNetService.importPetriNet(stream(SUPER_PARENT_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper()).net
        petriNetService.importPetriNet(stream(PARENT_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        // child extends version 1.1.0
        Process parentNet = petriNetService.importPetriNet(stream(PARENT_NET_FILE), VersionType.MINOR, superCreator.getLoggedSuper()).net
        Process childNet = petriNetService.importPetriNet(stream(CHILD_NET_FILE), VersionType.MINOR, superCreator.getLoggedSuper()).net

        Case aCase = workflowService.createCase(childNet.stringId, null, null, superCreator.getLoggedSuper()).getCase()
        assert aCase
        assert aCase.processIdentifier == childNet.identifier
        assert aCase.petriNetObjectId == childNet.objectId

        assert aCase.parentPetriNetIdentifiers.size() == 2
        assert aCase.parentPetriNetIdentifiers.get(0).identifier == superParentNet.identifier
        assert aCase.parentPetriNetIdentifiers.get(0).id == superParentNet.objectId
        assert aCase.parentPetriNetIdentifiers.get(1).identifier == parentNet.identifier
        assert aCase.parentPetriNetIdentifiers.get(1).id == parentNet.objectId

        assert aCase.dataSet.fields.size() == 4
        assert (aCase.dataSet.get("taskref2").value.value as List)[0] == aCase.tasks["t0"].taskId.toString()
    }
}
